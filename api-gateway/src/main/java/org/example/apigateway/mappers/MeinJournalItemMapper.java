package org.example.apigateway.mappers;

import org.example.apigateway.responses.articles.MeinJournalItem;

public class MeinJournalItemMapper {

    public static MeinJournalItem toMeinJournalItem(com.example.generated.MeinJournalItem meinJournalItem) {
        MeinJournalItem meinJournalItemModel = new MeinJournalItem();

        meinJournalItemModel.setId(meinJournalItem.getId());
        meinJournalItemModel.setTitle(meinJournalItem.getTitle());
        meinJournalItemModel.setUid(meinJournalItem.getUid());
        meinJournalItemModel.setPoints(meinJournalItem.getPoints());
        meinJournalItemModel.setIssn(meinJournalItem.getIssn());
        meinJournalItemModel.setEissn(meinJournalItem.getEissn());

        return meinJournalItemModel;
    }
}
