package it.polito.tdp.crimes.model;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.crimes.db.EventsDao;
import it.polito.tdp.crimes.model.Evento.TipoEvento;

public class Simulatore {
	
	//TIPI DI EVENTO 
	
		//1. Evento criminoso
		// 1.1 La centrale seleziona l'agente libero più vicino
		// 1.2 Se non ci sono disponibilità -> crimine mal gestito
		// 1.3 Se c'è un agente libero -> setto l'agente a occupato
		
		//2. L'agente selezionato ARRIVA sul posto
		// 2.1 Definisco quanto durerà l'intervento
		// 2.2 Controllo se il crimine è mal gestito (ritardo dell'agente)
		
		//3. Crimine TERMINATO
		// 3.1 "Libero" l'agente, che torna a essere disponibile
		
		//Strutture dati che ci servono
	
		// Input utente
		private Integer N;
		private Integer anno;
		private Integer mese;
		private Integer giorno;
		
		// Stato del sistema
		private Graph<District,DefaultWeightedEdge> grafo;
		private Map<District,Integer> agenti; //mappa: distretto -> #agenti liberi
		
		// Coda degli eventi
		private PriorityQueue<Evento> queue;
		// Output
		private Integer malGestiti;
		
		
		public void init(Integer N, Integer anno, Integer mese, Integer giorno,
				Graph<District,DefaultWeightedEdge> grafo) {
			this.N = N;
			this.anno = anno;
			this.mese = mese;
			this.giorno = giorno;
			this.grafo = grafo;
			
			this.malGestiti = 0;
			this.agenti = new HashMap<>();
			for(District d : this.grafo.vertexSet()) {
				this.agenti.put(d,0);
			}
			
			//devo scegliere dov'è la centrale (e mettere N agenti in quel distretto)
			EventsDao dao = new EventsDao();
			District minD = null;
			for(District d : grafo.vertexSet()) {
				if(d.getId().equals(dao.getDistrettoMin(anno))) {
					minD = d;
					break;
				}
			}
			this.agenti.put(minD, N);
			
			//creo e inizializzo la coda
			this.queue = new PriorityQueue<Evento>();
			
			for(Event e : dao.listAllEventsByDate(anno, mese, giorno)) {
				queue.add(new Evento(TipoEvento.CRIMINE, e.getReported_date(),e));
			}
		}
		
		public int run() {
			
			Evento e;
			
			while((e = queue.poll()) != null) {
			
				switch (e.getTipo()) {
				
				case CRIMINE:
					
					System.out.println("NUOVO CRIMINE! " + e.getCrimine().getIncident_id());
						
					//cerco l'agente libero più vicino
					District partenza = null;
					District distrettoCrimine = null; // distretto del crimine
					
					for(District d : grafo.vertexSet()) {
						if(d.getId().equals(e.getCrimine().getDistrict_id())) {
							distrettoCrimine = d; // lo recuperiamo
							break;
						}
					}
					
					// cerchiamo il distretto da dove parte agente più vicino al crimine
					partenza = cercaAgente(distrettoCrimine);
					
					if(partenza != null) {
							
						//c'è un agente libero in partenza
						//setto l'agente come occupato
						this.agenti.put(partenza, this.agenti.get(partenza) - 1);
							
						//cerco di capire quanto ci metterà l'agente libero ad arrivare sul posto
						Double distanza;
						if(partenza.equals(distrettoCrimine))
							distanza = 0.0;
						else
							distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(partenza, distrettoCrimine));
							
						Long seconds = (long) ((distanza * 1000)/(60/3.6));
							
						this.queue.add(new Evento(TipoEvento.ARRIVA_AGENTE, e.getData().plusSeconds(seconds), e.getCrimine()));
							
					} else {
							
						//non c'è nessun agente libero al momento -> crimine mal gestito
						System.out.println("CRIMINE " + e.getCrimine().getIncident_id() + " MAL GESTITO!");
						this.malGestiti ++;
					}
						
				break;
						
				case ARRIVA_AGENTE:
						
					System.out.println("ARRIVA AGENTE PER CRIMINE! " + e.getCrimine().getIncident_id());
						
					//CREIAMO METODO PER CALCOLARNE DURATA serve sapere la categoria
					Long duration = getDurata(e.getCrimine().getOffense_category_id());
						
					this.queue.add(new Evento(TipoEvento.GESTITO,e.getData().plusSeconds(duration), e.getCrimine()));
						
					//controllare se il crimine è mal gestito, SONO ARRIVATO DOPO + 15 MINUTI
					if(e.getData().isAfter(e.getCrimine().getReported_date().plusMinutes(15))) {
						System.out.println("CRIMINE " + e.getCrimine().getIncident_id() + " MAL GESTITO!");
						this.malGestiti ++;
					}
						
					break;
						
				case GESTITO:
						
					System.out.println("CRIMINE " + e.getCrimine().getIncident_id() + " GESTITO");
					
					District distrettoCrim = null; // distretto del crimine
					
					for(District d : grafo.vertexSet()) {
						if(d.getId().equals(e.getCrimine().getDistrict_id())) {
							distrettoCrim = d; // lo recuperiamo
							break;
						}
					}
					this.agenti.put(distrettoCrim, this.agenti.get(distrettoCrim)+1);
					break;
				}
			}
			
			return this.malGestiti;
		}

		private Long getDurata(String offense_category_id) {
			
			if(offense_category_id.equals("all_other_crimes")) {		
				Random r = new Random();
				if(r.nextDouble() > 0.5)
					return Long.valueOf(2*60*60);
				else
					return Long.valueOf(1*60*60);
			} else {
				return Long.valueOf(2*60*60);
			}
		}

		private District cercaAgente(District distrettoCrimine) {
			
			Double distanza = Double.MAX_VALUE;
			District distrettoPartenza = null; // distretto piu vicino a quello del crimine
			
			for(District d : this.agenti.keySet()) { // per ogni distretto del grafo
				
				if(this.agenti.get(d) > 0) { // vedo se ci sono agenti liberi nel distretto
				
					if(distrettoCrimine.equals(d)) { // se sono gia li, dist = 0 PERCHE NEL GRAFO
												// NON HO ARCO SELF LOOP
						return d;      
						
					// altrimenti confronto le distanze
					} else if(this.grafo.getEdgeWeight(this.grafo.getEdge(distrettoCrimine, d)) < distanza) {
						distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(distrettoCrimine, d));
						distrettoPartenza = d;
					}
				}
			}
			return distrettoPartenza;
		}
		
}
