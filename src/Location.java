public class Location
{
    private String location = "";
    private String latlng = "";
    
    public Location(String location, String latlng){
    	this.location = location;
    	this.latlng = latlng;
    	//this.latlng = null; //temporary, while testing, to reduce mem
    }

    public String getlocation() {
        return location;
    }

    public void setlocation(String location) {
        this.location = location;
    }
    
    public String getlatlng() {
        return latlng;
    }

    public void setlatlng(String latlng) {
        this.latlng = latlng;
    }
}
