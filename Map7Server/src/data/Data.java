package data;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import database.Column;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.EmptySetException;
import database.Example;
import database.TableData;
import database.TableSchema;

/**
 * Modella l'insieme di esempi di apprendimento
 */
@SuppressWarnings("serial")
public class Data implements Serializable {

	private static final int NUM_PAROLE_RIGA_SCHEMA = 2;
	private static final int NUM_PAROLE_RIGA_DESC_CONTINUOUS = 2;
	private static final int NUM_PAROLE_RIGA_DESC_DISCRETE = 3;
	private static final int NUM_PAROLE_RIGA_TARGET = 2;
	private static final int NUM_PAROLE_RIGA_DATA = 2;

	private List<Example> data = new ArrayList<Example>(); // Modificato da es. 6

	private int numberOfExamples; // Cardinalita'  del training set

	// NOTE: E' la riga letta nel traning set nel formato Attribute es motor
	// A,B,C,D,E
	private List<Attribute> explanatorySet = new LinkedList<Attribute>(); // Array di oggetti di tipo Attribute per
																			// rappresentare gli attributi
	// indipendenti di tipo discreto

	private ContinuousAttribute classAttribute; // Oggetto per modellare l'attributo di classe ContinuousAttribute

	// private Object data[][]; // Matrice nXm di tipo Object che contiene il
	// training set organizzato come numberOfExamples X numberAttribute

	public Data(String tableName) throws TrainingDataException {

		DbAccess db = new DbAccess();
		TableData td = new TableData(db);
		try {
			db.initConnection();
		} catch (DatabaseConnectionException e) {
			System.out.println(e);
			// TODO: testare!
			// Viene sollevata una eccezione di tipo TrainingDataException se la connessione
			// al database fallisce
			throw new TrainingDataException("La connessione al database non è andata a buon fine... riprova!");
		}

		Connection conn = db.getConnection();
		Statement s = null;
		try {
			s = conn.createStatement(); // Controllo se esiste la tabella...
			ResultSet r = s.executeQuery("SHOW TABLES LIKE '" + tableName + "'");
			if (r.next() == false) {
				// TODO: testare!
				throw new TrainingDataException("La tabella " + tableName + "non esiste.");
			}

			TableSchema ts = new TableSchema(db, tableName);

			//Controlliamo se il target è un valore numerico
			if ( ! ts.getColumn(ts.getNumberOfAttributes() - 1).isNumber() ) {
				throw new TrainingDataException(
					"[!] Errore! [!] La colonna contenente l'attributo di classe non è di tipo numerico.");
			}

			short iAttribute = 0;
			Iterator<Column> iter = ts.iterator();
			while(iter.hasNext()) {
				Column c = iter.next();
				if (isDiscreteAttributeType(c)) { // Attributi discreti
					Set<String> discreteValues = td.getDistinctColumnValues(tableName, c);

					explanatorySet.add(iAttribute,
							new DiscreteAttribute(c.getColumnName(), iAttribute, discreteValues));
				} else if ( ! isClassAttributeType(c) && isContinuousAttributeType(c)) { // Attributo continuo
					explanatorySet.add(iAttribute, new ContinuousAttribute(c.getColumnName(), iAttribute));
				} else { // Attributo di classe
					classAttribute = new ContinuousAttribute(c.getColumnName(), iAttribute);
				}
				iAttribute++;
			}
			// TODO: controllare un certo for -> for (int jColumn = 0; jColumn < s.length -
			// 1; jColumn++) e sostituirlo con un foreach su tutte le colonne in
			// explanatoryset come qui sotto

			try {
				data = td.getTransazioni(tableName);
				numberOfExamples = data.size();
			} catch (EmptySetException e) {
				new TrainingDataException("[!] Errore! [!] Errore nel training set: " + e);
			}
		} catch (SQLException e) {
			throw new TrainingDataException("[!] Errore! [!] La query specificata non è corretta.\nErrore: " + e);
		} // TODO: finally chiudere stream
		finally {
			//db.closeConnection();
		}
	}

	private boolean isDiscreteAttributeType(Column c) {
		return c.getTypeName().toLowerCase().contains("string");
	}

	private boolean isContinuousAttributeType(Column c) {
		return c.getTypeName().toLowerCase().contains("number");
	}

	private boolean isClassAttributeType(Column c) {
		return c.getColumnName().equals("C");
	}

	/**
	 * Metodo che restituisce la cardinalita'  del traning set in osservazione
	 *
	 * @return getNumberOfExamples
	 */
	public int getNumberOfExamples() {
		return numberOfExamples;
	}

	/**
	 * Metodo che restituisce la cardinalita'  degli attributi indipendenti
	 *
	 * @return length dell'array explanatorySet
	 */
	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	/**
	 * Restituisce il valore dell'attributo di classe per l'esempio exampleIndex
	 *
	 * @param int exampleIndex - indice di riga per la matrice data[][] per uno
	 *            specifico esempio
	 *
	 * @return double - valore dell'attributo di classe per l'esempio indicizzato in
	 *         input
	 */
	public Double getClassValue(int exampleIndex) {
		return (Double) data.get(exampleIndex).get(classAttribute.getIndex()); // TODO: vedere se funge
		// return (Double) data[exampleIndex][classAttribute.getIndex()];
	}

	/**
	 * Restituisce il valore dell'attributo indicizzato da attributeIndex per
	 * l'esempio exampleIndex
	 *
	 * @return data[exampleIndex][attributeIndex]
	 */

	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data.get(exampleIndex).get(attributeIndex); // TODO: vedere se funge

		// return data[exampleIndex][attributeIndex];
	}

	@Override
	public String toString() {
		String value = "";
		for (int i = 0; i < numberOfExamples; i++) {
			for (int j = 0; j < explanatorySet.size(); j++)
				value += data.get(i).get(j) + ",";
			// value += data[i][j] + ",";

			value += data.get(i).get(explanatorySet.size()) + "\n";
		}
		return value;

	}

	/**
	 * Ordina il sottoinsieme di esempi compresi nell'intervallo [beginExampleIndex,
	 * endExampleIndex] in data[][] rispetto allo specifico attributo attribute.
	 *
	 * @param attribute         Attributo i cui valori devono essere ordinati
	 * @param beginExampleIndex - indice che identifica il sotto-insieme di training
	 *                          coperto dal nodo corrente
	 * @param endExampleIndex   - indice che identifica il sotto-insieme di training
	 *                          coperto dal nodo corrente
	 */
	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {
		quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	// scambio esempio i con esempio j
	private void swap(int i, int j) {
		Object temp;
		for (int k = 0; k < getNumberOfExplanatoryAttributes() + 1; k++) {

			temp = data.get(i).get(k);
			data.get(i).set(k, data.get(j).get(k));
			data.get(j).set(k, temp);

//			temp = data[i][k];
//			data[i][k] = data[j][k];
//			data[j][k] = temp;
		}

	}

	/*
	 * Partiziona il vettore rispetto all'elemento x e restiutisce il punto di
	 * separazione
	 */
	private int partition(DiscreteAttribute attribute, int inf, int sup) {
		int i, j;

		i = inf;
		j = sup;
		int med = (inf + sup) / 2;
		String x = (String) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);

		while (true) {
			while (i <= sup && ((String) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;
			}
			while (((String) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;
			}

			if (i < j) {
				swap(i, j);
			} else
				break;
		}
		swap(inf, j);
		return j;
	}

	/*
	 * Partiziona il vettore rispetto all'elemento x e restiutisce il punto di
	 * separazione
	 */
	private int partition(ContinuousAttribute attribute, int inf, int sup) {
		int i, j;

		i = inf;
		j = sup;
		int med = (inf + sup) / 2;
		// TODO: cosa andava fatto? Non riusciva a fare cast da object a string / double

		// PROF:
		// Double x = (Double) getExplanatoryValue(med, attribute.getIndex());

		Double x = (Double) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);

		while (true) {
			while (i <= sup && ((Double) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;
			}

			while (((Double) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;
			}

			if (i < j) {
				swap(i, j);
			} else
				break;
		}
		swap(inf, j);
		return j;
	}

	/*
	 * Algoritmo quicksort per l'ordinamento di un array di interi A usando come
	 * relazione d'ordine totale "<="
	 */
	private void quicksort(Attribute attribute, int inf, int sup) {

		if (inf <= sup) {

			int pos;

			if (attribute instanceof DiscreteAttribute)
				pos = partition((DiscreteAttribute) attribute, inf, sup);
			else
				pos = partition((ContinuousAttribute) attribute, inf, sup);

			if ((pos - inf) < (sup - pos + 1)) {
				quicksort(attribute, inf, pos - 1);
				quicksort(attribute, pos + 1, sup);
			} else {
				quicksort(attribute, pos + 1, sup);
				quicksort(attribute, inf, pos - 1);
			}

		}

	}

	/**
	 * Restituisce l'attributo indicizzato da index in explanatorySet[]
	 *
	 * @param index Indice nell'array explanatorySet[] per uno specifico attributo
	 *              indipendente
	 * @return oggetto Attribute indicizzato da index
	 */
	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet.get(index);
	}

	/**
	 * Restituisce l'oggetto corrispondente all'attributo di classe
	 */
	private ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	private boolean isDouble(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}