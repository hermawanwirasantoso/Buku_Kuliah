package com.example.bukukuliah.ui.jadwal;

public class Jadwal {
    public String Hari_Kegiatan;
    public String Waktu_kegiatan_START;
    public String Waktu_kegiatan_END;
    public String Deskripsi_kegiatan;
    public String Judul_kegiatan;
    public String Lokasi_kegiatan;
    public String DayOfWeek;
    public String Jadwal_id;


    public Jadwal(){

    }

    public Jadwal(String judul_kegiatan, String deskripsi_kegiatan, String hari_Kegiatan, String waktu_kegiatan_START, String waktu_kegiatan_END, String lokasi_kegiatan, String dayofweek, String jadwal_id){
        this.Judul_kegiatan = judul_kegiatan;
        this.Deskripsi_kegiatan= deskripsi_kegiatan;
        this.Hari_Kegiatan = hari_Kegiatan;
        this.Waktu_kegiatan_START = waktu_kegiatan_START;
        this.Waktu_kegiatan_END = waktu_kegiatan_END;
        this.Lokasi_kegiatan = lokasi_kegiatan;
        this.DayOfWeek = dayofweek;
        this.Jadwal_id = jadwal_id;

    }


}
