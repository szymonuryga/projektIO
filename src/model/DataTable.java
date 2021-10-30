package model;

public class DataTable {
    private int[][] dataTable;

    public DataTable(int[][] dataTable) {
        this.dataTable = dataTable;
    }

    public int[][] getDataTable() {
        return dataTable;
    }

    public void setDataTable(int[][] dataTable) {
        this.dataTable = dataTable;
    }
}
