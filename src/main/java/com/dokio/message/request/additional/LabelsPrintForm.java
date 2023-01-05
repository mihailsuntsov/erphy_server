package com.dokio.message.request.additional;

import java.util.List;

public class LabelsPrintForm {

    private Long                        company_id;
    private List<LabelsPrintProduct>    labelsPrintProductsList;
    private int                         num_labels_in_row;
    private Long                        pricetype_id;
    private String                      file_name;

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public Long getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Long company_id) {
        this.company_id = company_id;
    }

    public List<LabelsPrintProduct> getLabelsPrintProductsList() {
        return labelsPrintProductsList;
    }

    public void setLabelsPrintProductsList(List<LabelsPrintProduct> labelsPrintProductsList) {
        this.labelsPrintProductsList = labelsPrintProductsList;
    }

    public int getNum_labels_in_row() {
        return num_labels_in_row;
    }

    public void setNum_labels_in_row(int num_labels_in_row) {
        this.num_labels_in_row = num_labels_in_row;
    }

    public Long getPricetype_id() {
        return pricetype_id;
    }

    public void setPricetype_id(Long pricetype_id) {
        this.pricetype_id = pricetype_id;
    }

    @Override
    public String toString() {
        return "LabelsPrintForm{" +
                "company_id=" + company_id +
                ", labelsPrintProductsList=" + labelsPrintProductsList +
                ", num_labels_in_row=" + num_labels_in_row +
                ", pricetype_id=" + pricetype_id +
                ", file_name='" + file_name + '\'' +
                '}';
    }
}
