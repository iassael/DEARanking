package net.ic3man.dearanking;

public class DMU {

	String code = "";
	String cat = "";
	String name = "";
	double area = 0.0;
	double lat = 0.0;
	double lon = 0.0;
	int C1 = 0;
	int C2 = 0;
	int rank = 0;
	double TE = 0.0;
	double D1 = 0.0;

	public DMU(String code, String cat, String name, double area, double lat,
			double lon, int c1, int c2) {
		super();
		this.code = code;
		this.cat = cat;
		this.name = name;
		this.area = area;
		this.lat = lat;
		this.lon = lon;
		C1 = c1;
		C2 = c2;
	}

	public DMU() {

	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public int getC1() {
		return C1;
	}

	public void setC1(int c1) {
		C1 = c1;
	}

	public int getC2() {
		return C2;
	}

	public void setC2(int c2) {
		C2 = c2;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public double getTE() {
		return TE;
	}

	public void setTE(double tE) {
		TE = tE;
	}

	public double getD1() {
		return D1;
	}

	public void setD1(double d1) {
		D1 = d1;
	}

}
