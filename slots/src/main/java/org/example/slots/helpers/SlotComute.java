    package org.example.slots.helpers;

    import com.example.generated.ItemForSlots;
    import com.example.generated.SlotAuthor;
    import com.example.generated.WorkerStatement;
    import org.example.slots.entities.PublicationKind;
    import org.example.slots.entities.SlotComputation;

    import java.math.BigDecimal;
    import java.math.RoundingMode;

    public final class SlotComute {

        private SlotComute() {}

        public static final int SCALE_SLOT = 6;
        public static final int SCALE_POINTS = 4;

        /**
         * Liczenie STRICT wg tabeli z Pc->U:
         * - Grupa A: U = 1/k
         * - Grupa B: U = sqrt(k/m) * 1/k
         * - Grupa C: U = 1/m
         *
         * k = liczba autorów "liczonych" w tej dyscyplinie/roku (np. mają statement)
         * m = liczba wszystkich autorów w osiągnięciu (wszyscy współautorzy)
         */
        public static SlotComputation computeSlotValueAndPointsRecalc(
                long ownerUserId,
                long disciplineId,
                int evalYear,
                PublicationKind kind,
                ItemForSlots item,
                WorkerStatement ownerSt,
                int k
        ) {
            int m = item.getAuthorsCount();
            if (m <= 0) throw new IllegalArgumentException("Publication has no authors (authorsCount<=0).");
            if (k <= 0) k = 1;

            BigDecimal points = bd(item.getPoints(), SCALE_POINTS);

            BigDecimal slotValue = computeUFromTable(kind, points, k, m);

            if (slotValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Computed slotValue<=0 (U). Check inputs k/m/points.");
            }
            if (slotValue.compareTo(BigDecimal.ONE) > 0) {
                slotValue = BigDecimal.ONE.setScale(SCALE_SLOT, RoundingMode.HALF_UP);
            }

            BigDecimal pointsRecalc = points.multiply(slotValue).setScale(SCALE_POINTS, RoundingMode.HALF_UP);
            return new SlotComputation(points, slotValue, pointsRecalc);
        }

        public static BigDecimal computeUFromTable(PublicationKind kind, BigDecimal points, int k, int m) {
            if (k <= 0 || m <= 0) return BigDecimal.ZERO;

            BigDecimal oneOverK = BigDecimal.ONE.divide(BigDecimal.valueOf(k), SCALE_SLOT, RoundingMode.HALF_UP);
            BigDecimal oneOverM = BigDecimal.ONE.divide(BigDecimal.valueOf(m), SCALE_SLOT, RoundingMode.HALF_UP);

            BigDecimal kOverM = BigDecimal.valueOf(k)
                    .divide(BigDecimal.valueOf(m), SCALE_SLOT, RoundingMode.HALF_UP);
            BigDecimal sqrtKOverM = sqrtBd(kOverM);
            BigDecimal sqrtKOverM_times_1overK = sqrtKOverM.multiply(oneOverK).setScale(SCALE_SLOT, RoundingMode.HALF_UP);

            int pc = points.setScale(0, RoundingMode.HALF_UP).intValue();

            return switch (kind) {
                case CHAPTER -> {
                    if (pc >= 50) yield oneOverK;                // 50 -> 1/k
                    if (pc == 20) yield sqrtKOverM_times_1overK; // 20 -> sqrt(k/m)*1/k
                    yield oneOverM;                              // 5 -> 1/m
                }
                case MONOGRAPH -> {
                    if (pc >= 100) yield oneOverK;               // 200/100 -> 1/k
                    if (pc == 80) yield sqrtKOverM_times_1overK; // 80 -> sqrt(k/m)*1/k
                    yield oneOverM;                              // 20 -> 1/m
                }
                case ARTICLE -> {
                    // zgodnie z Twoimi przykładami:
                    if (pc == 70 || pc == 40) yield sqrtKOverM_times_1overK; // grupa B
                    if (pc == 20 || pc == 5)  yield oneOverM;                // grupa C
                    yield oneOverK;                                          // grupa A (np. 100/140/200)
                }
            };
        }


        public static boolean isMono(PublicationKind k) {
            return k == PublicationKind.MONOGRAPH || k == PublicationKind.CHAPTER;
        }

        public static BigDecimal bd(double v, int scale) {
            return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
        }

        public static BigDecimal nz(BigDecimal v) {
            return v == null ? BigDecimal.ZERO : v;
        }

        private static BigDecimal sqrtBd(BigDecimal x) {
            double d = x.doubleValue();
            if (d <= 0.0) return BigDecimal.ZERO.setScale(SCALE_SLOT, RoundingMode.HALF_UP);
            return BigDecimal.valueOf(Math.sqrt(d)).setScale(SCALE_SLOT, RoundingMode.HALF_UP);
        }
    }
