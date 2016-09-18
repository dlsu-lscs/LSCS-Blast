package kewpe.lscsblaster;

public class Filter {
    private String filter;
    private boolean isSelected;

    public Filter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String idNumber) {
        this.filter = idNumber;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
