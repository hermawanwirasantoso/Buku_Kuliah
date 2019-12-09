package com.example.bukukuliah.ui.jadwal;

public class Jadwal {
    public String Hari_Kegiatan;
    public String Waktu_kegiatan;
    public String Deskripsi_kegiatan;
    public String Judul_kegiatan;
    public String Lokasi_kegiatan;


    public Jadwal(){

    }

    public Jadwal(String judul_kegiatan, String deskripsi_kegiatan, String hari_Kegiatan, String waktu_kegiatan, String lokasi_kegiatan){
        this.Judul_kegiatan = judul_kegiatan;
        this.Deskripsi_kegiatan= deskripsi_kegiatan;
        this.Hari_Kegiatan = hari_Kegiatan;
        this.Waktu_kegiatan = waktu_kegiatan;
        this.Lokasi_kegiatan = lokasi_kegiatan;
    }


    //getter

    public String getHari_Kegiatan() {
        return Hari_Kegiatan;
    }

    public String getWaktu_kegiatan() {
        return Waktu_kegiatan;
    }

    public String getDeskripsi_kegiatan() {
        return Deskripsi_kegiatan;
    }

    public String getJudul_kegiatan() {
        return Judul_kegiatan;
    }

    public String getLokasi_kegiatan() {
        return Lokasi_kegiatan;
    }

    //setter

    public void setHari_Kegiatan(String hari_Kegiatan) {
        Hari_Kegiatan = hari_Kegiatan;
    }

    public void setWaktu_kegiatan(String waktu_kegiatan) {
        Waktu_kegiatan = waktu_kegiatan;
    }

    public void setDeskripsi_kegiatan(String deskripsi_kegiatan) {
        Deskripsi_kegiatan = deskripsi_kegiatan;
    }

    public void setJudul_kegiatan(String judul_kegiatan) {
        Judul_kegiatan = judul_kegiatan;
    }

    public void setLokasi_kegiatan(String lokasi_kegiatan) {
        Lokasi_kegiatan = lokasi_kegiatan;
    }
}
