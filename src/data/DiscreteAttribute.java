package data;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Rappresenta un attributo discreto
 */
//NOTE: Attributi nominali (es. A, B, C...)
public class DiscreteAttribute extends Attribute implements Iterable<String> {

	// Rappresenta l'insieme di valori discreti che l'attributo può assumere
	private Set<String> values = new TreeSet<String>(); // order by asc

	/**
	 * Invoca il costruttore della super-classe e avvalora l'array values[] con i
	 * valori discreti in input.
	 *
	 * @param name   Nome simbolico dell'attributo
	 * @param index  Indice dell'attributo
	 * @param values Valori discreti
	 */
	DiscreteAttribute(String name, int index, Set<String> values) {
		super(name, index);
		this.values = values;
	}

	/**
	 *
	 * @return Cardinalita' dell'array values (numero di valori discreti)
	 */
	int getNumberOfDistinctValues() {
		return values.size();
	}

	/**
	 * Metodo che prende in input un indice i di un solo valore discreto
	 *
	 * @param i
	 * @return i-esimo valore discreto dell'array values[]
	 */
	String getValue(int i) {

 		Iterator<String> iter = iterator();

		int j = 0;
		while (j < i) {
			// Non è stato inserito il controllo per verificare se c'è un elemento
			// successivo poichè non crediamo sia possibile
			// modificare la signature del metodo.
			// TODO throws exception
			iter.next();
			j++;
		}
		return iter.next();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public Iterator<String> iterator() {
		return values.iterator();
	}
}
