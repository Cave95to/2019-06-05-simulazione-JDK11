package it.polito.tdp.crimes.model;

import com.javadocmd.simplelatlng.LatLng;

public class District {
	
	private Integer id;
	private double longitud;
	private double latitud;
	
	private LatLng coord;
	
	public District(Integer id, double longitud, double latitud) {
		super();
		this.id = id;
		this.longitud = longitud;
		this.latitud = latitud;
		this.coord = new LatLng(latitud, longitud);
		
	}
	@Override
	public String toString() {
		return "District [id=" + id + ", longitud=" + longitud + ", latitud=" + latitud + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		District other = (District) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public double getLongitud() {
		return longitud;
	}
	public void setLongitud(double longitud) {
		this.longitud = longitud;
	}
	public double getLatitud() {
		return latitud;
	}
	public void setLatitud(double latitud) {
		this.latitud = latitud;
	}
	public LatLng getCoord() {
		return coord;
	}
	public void setCoord(LatLng coord) {
		this.coord = coord;
	}
	

}
