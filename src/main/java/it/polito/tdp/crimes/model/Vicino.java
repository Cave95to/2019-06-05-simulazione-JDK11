package it.polito.tdp.crimes.model;

public class Vicino implements Comparable<Vicino> {
	
	private District d;
	private Double dist;
	
	public Vicino(District d, Double dist) {
		super();
		this.d = d;
		this.dist = dist;
	}
	@Override
	public String toString() {
		return "Vicino [d=" + d + ", dist=" + dist + "]";
	}
	public District getD() {
		return d;
	}
	public void setD(District d) {
		this.d = d;
	}
	public Double getDist() {
		return dist;
	}
	public void setDist(Double dist) {
		this.dist = dist;
	}
	@Override
	public int compareTo(Vicino o) {
		// TODO Auto-generated method stub
		return this.dist.compareTo(o.dist);
	}
	
	
}
