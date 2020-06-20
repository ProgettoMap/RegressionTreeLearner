package data;

/**
 * La classe modella un generico attributo discreto o continuo.
 * 
 */
public abstract class Attribute {

	private String name; // Nome simbolico dell'attributo
	private int index; // Identificativo numerico dell'attributo

	/**
	 * E' il costruttore di classe. Inizializza i valori dei membri name, index
	 * 
	 * @param name
	 * @param index
	 */
	Attribute(String name, int index) {

		this.index = index;
		this.name = name;

	}

	/**
	 * Restituisce il valore nel membro name;
	 * 
	 * @return name
	 */
	String getName() {
		return name;
	}

	/**
	 * Restituisce il valore nel membro index;
	 * 
	 * @return index
	 */
	public int getIndex() {
		return index;
	}

}
