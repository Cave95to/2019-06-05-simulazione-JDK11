package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private EventsDao dao;
	private SimpleWeightedGraph<District, DefaultWeightedEdge > grafo;
	private List<District> vertici;
	
	
	public Model() {
		
		this.dao = new EventsDao();
	}
	
	public List<Integer> getAnni() {
		return this.dao.getAnni();
	}

	public void creaGrafo(Integer anno) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		this.vertici = new ArrayList<>();
		
		this.vertici = this.dao.getVertici(anno);
		
		Graphs.addAllVertices(this.grafo, this.dao.getVertici(anno));
		
		for(District d1: this.vertici) {
			for(District d2 : this.vertici) {
				if(!d1.equals(d2) && !this.grafo.containsEdge(d2, d1)) {
					Graphs.addEdge(this.grafo, d1, d2, LatLngTool.distance(d1.getCoord(), d2.getCoord(), LengthUnit.KILOMETER));
				}
			}
		}
		
	}
	
	public int getNVertici() {
		return this.vertici.size();
	}
	
	public int getNArchi() {
		return this.grafo.edgeSet().size();
	}

	public List<District> getVertici() {
		
		return this.vertici;
	}

	public List<Vicino> getVicini(District d) {
		
		List<Vicino> result = new ArrayList<>();
		
		for(District di : Graphs.neighborListOf(this.grafo, d)) {
			result.add(new Vicino(di, this.grafo.getEdgeWeight(this.grafo.getEdge(di, d))));
		}
		
		Collections.sort(result);
		return result;
	}

	public Integer simula(Integer anno, Integer mese, Integer giorno, Integer n) {
		Simulatore sim = new Simulatore();
		sim.init(n, anno, mese, giorno, grafo);
		return sim.run();
	}

	public List<Integer> getMesi() {
		// TODO Auto-generated method stub
		return dao.getMesi();
	}

	public List<Integer> getGiorni() {
		// TODO Auto-generated method stub
		return dao.getGiorni();
	}
}
