package Unlike.tabatmie.Dto;

public class ProgressDTO {

    String cnt;
    boolean center;

    public ProgressDTO(String cnt, boolean center) {
        this.cnt = cnt;
        this.center = center;
    }

    public String getCnt() {
        return cnt;
    }

    public void setCnt(String cnt) {
        this.cnt = cnt;
    }

    public boolean isCenter() {
        return center;
    }

    public void setCenter(boolean center) {
        this.center = center;
    }
}
