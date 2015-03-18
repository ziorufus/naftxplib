/**
 * TXPtoNAF
 * Add tlinks and clinks layers in NAF.
 * use version 1.0.2 of kaflib (from EHU)
 * version: 2.1
 */

package eu.fbk.newsreader.naf;

import ixa.kaflib.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;


public class TXPtoNAF {

	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String JAXP_SCHEMA_LOCATION = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	static String NULLVALUE = "__NULL__";


	static HashMap<String, Integer> intCol = new HashMap<String, Integer>();

	static int endHeaderLine;

	static BufferedWriter buffout = null;
	static private String encodingOUT = "UTF8";

	public static String TXP2NAF(File f, String[][] lines, String beginTimeSpan, String eltName) throws IOException {
		KAFDocument nafFile = KAFDocument.createFromFile(f);
		return TXP2NAF(nafFile, lines, beginTimeSpan, eltName);
	}

	public static String TXP2NAF(KAFDocument nafFile, String[][] lines, String beginTimeSpan, String eltName) throws IOException {

		HashMap<String, String> termIdCoeventId = new HashMap<String, String>();
		HashMap<String, Coref> coeventIdElt = new HashMap<String, Coref>();
		HashMap<String, Timex3> timexIdElt = new HashMap<String, Timex3>();

		ListIterator<Coref> corefl = nafFile.getCorefs().listIterator();
		while (corefl.hasNext()) {
			Coref co = corefl.next();
			if (co.getId().startsWith("coevent")) {
				coeventIdElt.put(co.getId(), co);

				ListIterator<Span<Term>> spanl = co.getSpans().listIterator();
				while (spanl.hasNext()) {
					Span<Term> s = spanl.next();
					ListIterator<Term> tarl = s.getTargets().listIterator();


					while (tarl.hasNext()) {
						Term tar = tarl.next();
						termIdCoeventId.put(tar.getId(), co.getId());
					}
				}
			}
		}

		HashMap<String, String> termIdPredId = new HashMap<String, String>();
		HashMap<String, Predicate> predIdElt = new HashMap<String, Predicate>();

		ListIterator<Predicate> predl = nafFile.getPredicates().listIterator();
		while (predl.hasNext()) {
			Predicate pred = predl.next();
			predIdElt.put(pred.getId(), pred);

			Span<Term> s = pred.getSpan();
			ListIterator<Term> tarl = s.getTargets().listIterator();

			while (tarl.hasNext()) {
				Term tar = tarl.next();
				termIdPredId.put(tar.getId(), pred.getId());
			}
		}


		ListIterator<Timex3> timexl = nafFile.getTimeExs().listIterator();
		while (timexl.hasNext()) {
			Timex3 tx = timexl.next();
			timexIdElt.put(tx.getId(), tx);
		}

		if (eltName.equals("TLINK")) {
			//nafFile.removeLayer(KAFDocument.Layer.tlinks);
			int cptTlink = 0;
			for (int i = 0; i < lines.length; i++) {
				String from = lines[i][0];
				String to = lines[i][1];
				TLinkReferable newfrom = null;
				TLinkReferable newto = null;

				if (from != null && !from.equals("") && to != null && !to.equals("")) {

					if (termIdPredId.containsKey(from.replace("e", "t"))) {
						from = termIdPredId.get(from.replace("e", "t"));
						newfrom = predIdElt.get(from);
					}
				/*if(termIdCoeventId.containsKey(from.replace("e","t"))){
					from = termIdCoeventId.get(from.replace("e","t"));
					newfrom = coeventIdElt.get(from);
				}*/
					else {
						newfrom = timexIdElt.get(from);
					}

					if (termIdPredId.containsKey(to.replace("e", "t"))) {
						to = termIdPredId.get(to.replace("e", "t"));
						newto = predIdElt.get(to);
					}
				/*if(termIdCoeventId.containsKey(to.replace("e","t"))){
					to = termIdCoeventId.get(to.replace("e", "t"));
					newto = coeventIdElt.get(to);
				}*/
					else {
						newto = timexIdElt.get(to);
					}

					String tlinkid = "tlink" + Integer.toString(cptTlink);
					String relType = lines[i][lines[i].length - 1];
					String fromType = "event";
					String toType = "event";

					if (from.startsWith("tmx")) {
						fromType = "timex";
					}
					if (to.startsWith("tmx")) {
						toType = "timex";
					}

					if (relType != null && !relType.equals("O")) {
						nafFile.newTLink(tlinkid, newfrom, newto, relType);
						//nafFile.createTlink(tlinkid, newfrom, newto, relType, fromType, toType);
					}

					cptTlink++;
				}
			}


//			if (beginTimeSpan != null) {
//				LinguisticProcessor lp = nafFile.addLinguisticProcessor("temporalRelations", "TempRelPro-FBK");
//				lp.setVersion("2.0.0");
//				lp.setBeginTimestamp(beginTimeSpan);
//				lp.setEndTimestamp(getTodayDate());
//			}
		}

		if (eltName.equals("CLINK")) {

			int cptClink = 0;
			for (int i = 0; i < lines.length; i++) {
				String from = lines[i][0];
				String to = lines[i][1];
				if (from != null && !from.equals("") && to != null && !to.equals("")) {
				
				/*if(termIdCoeventId.containsKey(from.replace("e","t"))){
					from = termIdCoeventId.get(from.replace("e","t"));
				}
				if(termIdCoeventId.containsKey(to.replace("e","t"))){
					to = termIdCoeventId.get(to.replace("e", "t"));
				}*/

					if (termIdPredId.containsKey(from.replace("e", "t"))) {
						from = termIdPredId.get(from.replace("e", "t"));
					}
					if (termIdPredId.containsKey(to.replace("e", "t"))) {
						to = termIdPredId.get(to.replace("e", "t"));
					}

					String clinkid = "clink" + Integer.toString(cptClink);
					String relType = lines[i][lines[i].length - 2];
					String orderRel = lines[i][lines[i].length - 1];

					if (orderRel.equals("CLINK-R")) {
						String temp = from;
						from = to;
						to = temp;
					}

					if (!relType.matches("[A-Z]+")) {
						relType = "CAUSE";
					}

					if (relType.equals("O")) {
						relType = "";
					}

					if (relType != null && !relType.equals("O")) {
						nafFile.newCLink(clinkid, predIdElt.get(from), predIdElt.get(to));
						//nafFile.createClink(clinkid, from, to, relType);
					}

					cptClink++;
				}
			}

//			LinguisticProcessor lp = nafFile.addLinguisticProcessor("causalRelations", "CausalRelPro-FBK");
//			lp.setVersion("1.0.0");
//			lp.setBeginTimestamp(beginTimeSpan);
//			lp.setEndTimestamp(getTodayDate());
		}

		return nafFile.toString();
	}

	public static String getTodayDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		Date date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//BufferedReader br = new BufferedReader(new FileReader(args[0]));
		//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int nbCol = 3;
		String beginTimeSpan = Long.toString(System.currentTimeMillis());
		if (args.length > 2) {
			beginTimeSpan = args[2];
		}

		String eltName = "TLINK";
		if (args.length > 3) {
			eltName = args[3];
			if (eltName.equals("CLINK")) {
				nbCol += 1;
			}
		}
		String[][] lines = TextProFileFormat.readFileTextPro(args[1], nbCol, true);
		//TXP2NAF(new File(args[0]), args[2], lines);
		System.out.println(TXP2NAF(new File(args[0]), lines, beginTimeSpan, eltName));
	}

}
