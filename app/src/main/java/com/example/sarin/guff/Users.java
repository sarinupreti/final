package com.example.sarin.guff;

public class Users {

    private String Name;
    private String Image;
    private String Status;
    private String Thumb_image;
    private String Id;


    public Users(){

    }

    public Users(String Name, String Image, String Status, String Thumb_image, String Id) {
        this.Name = Name;
        this.Image = Image;
        this.Status = Status;
        this.Thumb_image = Thumb_image;
        this.Id = Id;
    }

    public String getName() {
        return Name;
    }
    public String getImage() {
        return Image;
    }
    public String getStatus() {
        return Status;
    }
    public String getThumb_image(){
        return Thumb_image;
    }
    public String getId() {
        return Id;
    }
}
