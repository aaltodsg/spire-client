package com.spire.parking_details;

/**
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * E-mail: volodymyr.n.paliy@gmail.com
 * Date: 18.07.13
 * Time: 16:30
 */
public class ListItems {

    int ImageResource;
    String Description;

    public ListItems ( int ImageResource, String Description ){
        this.ImageResource = ImageResource;
        this.Description = Description;
    }


    /**/
    //setters

    public void setImageResource ( int imageResource){
        this.ImageResource = imageResource;
    }

    public void setDescription ( String description ){
        this.Description = description;
    }


    /**/

    //getters
    public int getImageResource ( ){
        return this.ImageResource;
    }

    public String getDescription (){
        return this.Description;
    }
}
