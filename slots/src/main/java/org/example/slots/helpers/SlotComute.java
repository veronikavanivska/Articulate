package org.example.slots.helpers;

import com.example.generated.ItemForSlots;
import com.example.generated.SlotAuthor;
import com.example.generated.WorkerStatement;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.example.slots.clients.ProfilesClient;
import org.example.slots.entities.AuthorsInDisciplineCtx;
import org.example.slots.entities.PublicationKind;
import org.example.slots.entities.SlotComputation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * SlotComute (helper) — central place for slot math and BigDecimal utilities.
 *
 * =========================
 * SLOT MODEL (FORMULAS)
 * =========================
 *
 * Definitions:
 *  - m = total number of authors (authorsCount from ItemForSlots)
 *  - A = authors that "count" in given discipline+year:
 *        internal authors for whom Profiles has a statement in (disciplineId, evalYear)
 *  - k = |A|
 *  - w_i = slotInDiscipline for author i (usually 1.0 in your simplified model)
 *  - W = Σ w_i over A
 *
 * Owner share inside discipline:
 *  - share(owner) = w_owner / W
 *
 * Publication factor:
 *  - factor = slotFactor(kind, points, k, m)
 *
 * Final slot consumption for owner:
 *  - slotValue = share(owner) * factor
 *
 * Recalculated points:
 *  - pointsRecalc = points * slotValue
 *
 * IMPORTANT EFFECT:
 *  - If a coauthor is NOT assigned to this discipline/year (no statement),
 *    they do NOT enter A → they do NOT reduce owner share → slotValue stays high.
 *    (This fixes your “200pts monograph + coauthor from other discipline” case.)
 */
@Component
public class SlotComute {



        /** Scale for slot computations (e.g., slotValue). */
        public static final int SCALE_SLOT = 6;

        /** Scale for points computations (e.g., pointsRecalc). */
        public static final int SCALE_POINTS = 4;

        /** Points thresholds for factor rules. */
        public static final BigDecimal THRESH_HIGH = BigDecimal.valueOf(100);
        public static final BigDecimal THRESH_MID  = BigDecimal.valueOf(40);


        /**
         * Compute slotValue and pointsRecalc for a single item under a discipline/year,
         * for the "owner" (request user).
         *
         * @throws StatusRuntimeException when computation is impossible (no eligible authors, etc.)
         */
        public static SlotComputation computeSlotValueAndPointsRecalc(
                long ownerUserId,
                long disciplineId,
                int evalYear,
                PublicationKind kind,
                ItemForSlots item,
                WorkerStatement ownerSt
        ) {
            int m = item.getAuthorsCount();
            if (m <= 0) {
                throw Status.INVALID_ARGUMENT.withDescription("Publication has no authors.").asRuntimeException();
            }

            BigDecimal points = bd(item.getPoints(), SCALE_POINTS);

            // w_owner: slotInDiscipline from statement (fallback to 1)
            BigDecimal ownerW = bd(ownerSt.getSlotInDiscipline(), SCALE_SLOT);
            if (ownerW.compareTo(BigDecimal.ZERO) <= 0) ownerW = BigDecimal.ONE;

            AuthorsInDisciplineCtx ctx = computeEligibleAuthorsCtx(ownerUserId, disciplineId, evalYear, item, ownerW);

            // share = w_owner / sumW
            BigDecimal share = ownerW.divide(ctx.sumW(), SCALE_SLOT, RoundingMode.HALF_UP);

            // factor depends on kind, points class, and (k,m)
            BigDecimal factor = slotFactor(kind, points, ctx.k(), m);

            // slotValue = share * factor
            BigDecimal slotValue = share.multiply(factor).setScale(SCALE_SLOT, RoundingMode.HALF_UP);

            if (slotValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw Status.FAILED_PRECONDITION
                        .withDescription("Computed slotValue<=0. Check discipline assignments/statements.")
                        .asRuntimeException();
            }

            // defensive clamp
            if (slotValue.compareTo(BigDecimal.ONE) > 0) {
                slotValue = BigDecimal.ONE.setScale(SCALE_SLOT, RoundingMode.HALF_UP);
            }

            BigDecimal pointsRecalc = points.multiply(slotValue).setScale(SCALE_POINTS, RoundingMode.HALF_UP);

            return new SlotComputation(points, slotValue, pointsRecalc);
        }

        // =========================================================
        // Core helpers (authors eligibility + factor)
        // =========================================================

        /**
         * Builds (k, sumW) for authors eligible in given discipline/year.
         * An eligible author is an internal author for which Profiles can return a statement.
         *
         * Defensive rule: if owner is missing from eligible set, we add ownerW anyway.
         */
        private static AuthorsInDisciplineCtx computeEligibleAuthorsCtx(
                long ownerUserId,
                long disciplineId,
                int evalYear,
                ItemForSlots item,
                BigDecimal ownerW
        ) {
            BigDecimal sumW = BigDecimal.ZERO;
            int k = 0;
            boolean ownerSeen = false;

            for (SlotAuthor a : item.getAuthorsList()) {
                long uid = a.getUserId();
                if (uid <= 0) continue; // external

                try {
                    WorkerStatement st = ProfilesClient.getOrCreateStatement(uid, disciplineId, evalYear).getStatement();

                    BigDecimal w = bd(st.getSlotInDiscipline(), SCALE_SLOT);
                    if (w.compareTo(BigDecimal.ZERO) <= 0) w = BigDecimal.ONE;

                    sumW = sumW.add(w);
                    k++;

                    if (uid == ownerUserId) {
                        ownerSeen = true;
                    }
                } catch (StatusRuntimeException ignore) {
                    // author not assigned to this discipline/year -> not counted
                }
            }

            // owner must be countable; if not present, include them
            if (!ownerSeen) {
                sumW = sumW.add(ownerW);
                k++;
            }

            if (k <= 0 || sumW.compareTo(BigDecimal.ZERO) <= 0) {
                throw Status.FAILED_PRECONDITION
                        .withDescription("No authors assigned to this discipline/year. Cannot compute slotValue.")
                        .asRuntimeException();
            }

            return new AuthorsInDisciplineCtx(k, sumW);
        }

        /**
         * factor rules:
         *
         * ARTICLE:
         *  - points >= 100        -> factor = 1
         *  - 40 <= points < 100   -> factor = sqrt(k/m)
         *  - points < 40          -> factor = k/m
         *
         * MONOGRAPH / CHAPTER:
         *  - points >= 100        -> factor = 1
         *  - otherwise            -> factor = k/m
         */
        private static BigDecimal slotFactor(PublicationKind kind, BigDecimal points, int k, int m) {
            if (k <= 0 || m <= 0) return BigDecimal.ZERO;

            BigDecimal kOverM = BigDecimal.valueOf(k)
                    .divide(BigDecimal.valueOf(m), SCALE_SLOT, RoundingMode.HALF_UP);

            return switch (kind) {
                case ARTICLE -> {
                    if (points.compareTo(THRESH_HIGH) >= 0) {
                        yield BigDecimal.ONE;
                    } else if (points.compareTo(THRESH_MID) >= 0) {
                        yield sqrtBd(kOverM);
                    } else {
                        yield kOverM;
                    }
                }
                case MONOGRAPH, CHAPTER -> {
                    if (points.compareTo(THRESH_HIGH) >= 0) {
                        yield BigDecimal.ONE;
                    } else {
                        yield kOverM;
                    }
                }
            };
        }

        private static BigDecimal sqrtBd(BigDecimal x) {
            double d = x.doubleValue();
            if (d <= 0.0) return BigDecimal.ZERO.setScale(SCALE_SLOT, RoundingMode.HALF_UP);
            return BigDecimal.valueOf(Math.sqrt(d)).setScale(SCALE_SLOT, RoundingMode.HALF_UP);
        }

        // =========================================================
        // BigDecimal utilities (used by SlotService)
        // =========================================================

        public static BigDecimal bd(double v, int scale) {
            return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
        }

        public static BigDecimal nz(BigDecimal v) {
            return v == null ? BigDecimal.ZERO : v;
        }

        public static boolean isMono(PublicationKind kind) {
            return kind == PublicationKind.MONOGRAPH || kind == PublicationKind.CHAPTER;
        }



    }


