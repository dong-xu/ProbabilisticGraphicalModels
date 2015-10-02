import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import norsys.netica.Environ;
import norsys.netica.Net;
import norsys.netica.NeticaException;
import norsys.netica.Streamer;
import norsys.neticaEx.aliases.Node;

public class XuDongBayesian {

	public static void main(String args[]) throws NeticaException {

		Node.setConstructorClass("norsys.neticaEx.aliases.Node");
		new Environ(null);
		Net net = new Net();
		net.setName("BayesianNet");
		// H: overall health(1EXCELLENT,2 VERY GOOD,3 GOOD,4 FAIR,5 POOR)
		Node H = new Node("H", "a1,a2,a3,a4,a5", net);
		// HA: heart attack since last year(1YES,2NO)
		Node HA = new Node("HA", "a1,a2", net);
		// BH: Broken/fractured hip since last year
		Node BH = new Node("BH", "a1,a2", net);
		// BB: Broken/fractured bones (other) since last year
		Node BB = new Node("BB", "a1,a2", net);
		// HS: Hospital Stay since last year
		Node HS = new Node("HS", "a1,a2", net);
		// KS: Knee surgery since last year
		Node KS = new Node("KS", "a1,a2", net);
		// HIS: Hip surgery since last year
		Node HIS = new Node("HIS", "a1,a2", net);
		// BS: Back surgery since last year
		Node BS = new Node("BS", "a1,a2", net);
		// HAS: heart surgery since last year
		Node HAS = new Node("HAS", "a1,a2", net);
		// FS: fall in last month
		Node FS = new Node("FS", "a1,a2", net);
		// WF: worry about falling in last month
		Node WF = new Node("WF", "a1,a2", net);
		// DD: depression, hopelessness in last month(1 NOT AT ALL,2 SEVERAL
		// DAYS, 3 MORE THAN HALF THE DAYS, 4 NEARLY EVERY DAY)
		Node DD = new Node("DD", "a1,a2,a3,a4", net);
		// DN: nervous/anxious in last month
		Node DN = new Node("DN", "a1,a2,a3,a4", net);
		// DW: can't stop worrying in last month
		Node DW = new Node("DW", "a1,a2,a3,a4", net);
		// SL: more than 30 mins to sleep in last month(1 EVERY NIGHT (7
		// NIGHTS A WEEK),2 MOST NIGHTS (5-6 NIGHTS A WEEK),3 SOME NIGHTS
		// (2-4 NIGHTS A WEEK),4 RARELY (ONCE A WEEK OR LESS),5 NEVER)
		Node SL = new Node("SL", "a1,a2,a3,a4,a5", net);
		// SM: take sleep medication in last month
		Node SM = new Node("SM", "a1,a2,a3,a4,a5", net);
		DD.addLink(H);
		HA.addLink(H);
		BB.addLink(H);
		BH.addLink(H);
		HS.addLink(H);
		WF.addLink(FS);
		BS.addLink(FS);
		BS.addLink(BB);
		BB.addLink(FS);
		KS.addLink(FS);
		BH.addLink(FS);
		HS.addLink(KS);
		HS.addLink(BS);
		HS.addLink(HIS);
		HIS.addLink(BH);
		HAS.addLink(HS);
		HA.addLink(HAS);
		DN.addLink(BB);
		DN.addLink(WF);
		DN.addLink(BH);
		DN.addLink(DD);
		DN.addLink(HA);
		SL.addLink(DW);
		SL.addLink(DN);
		SL.addLink(DD);
		SL.addLink(HS);
		SL.addLink(HIS);
		DW.addLink(BB);
		DW.addLink(BH);
		DW.addLink(HA);
		DW.addLink(DN);
		DD.addLink(BB);
		DD.addLink(BH);
		DD.addLink(HA);
		DD.addLink(HS);
		SM.addLink(SL);
		// prepare reader and writer
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			// initialize the writer and reader
			reader = new BufferedReader(new FileReader("dataprocess.csv"));
			writer = new BufferedWriter(new FileWriter("trainingFile.csv"));
			String line = "";
			int index = 0;
			// read line by line from the data
			while ((line = reader.readLine()) != null) {
				// split the data by comma
				String[] states = line.split(",");
				StringBuffer sb = new StringBuffer();
				// rewriter the data by add "a" the data and separate the data
				// by space
				for (String state : states)
					if (index == 0)
						sb.append(state).append(" ");
					else
						sb.append("a").append(state).append(" ");
				writer.write(sb.toString());
				writer.newLine();
				index++;
			}
			// flush the data into disk before close the writer
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// begin to training the data, reload the data from file to project
		Streamer caseFile = new Streamer("trainingFile.csv");
		net.reviseCPTsByCaseFile(caseFile, net.getNodes(), 1.0);
		// compile the result
		net.compile();

		// reload the whole data from the data
		List<String> wholeSet = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader("trainingFile.csv"));
			String line = "";
			while ((line = reader.readLine()) != null)
				wholeSet.add(line);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Random pick the test data
		List<String> testSet = new ArrayList<String>();
		Random random = new Random();
		for (int i = 0; i < 1000; i++) {
			int randomNum = random.nextInt(wholeSet.size() - 1) + 1;
			testSet.add(wholeSet.get(randomNum));
		}
		// Add observation to
		int sameNumber = 0;
		for (String oCase : testSet) {
			String[] states = oCase.split(" ");
			H.finding().enterState(states[0]);
			HA.finding().enterState(states[1]);
			BH.finding().enterState(states[2]);
			BB.finding().enterState(states[3]);
			HS.finding().enterState(states[4]);
			KS.finding().enterState(states[5]);
			HIS.finding().enterState(states[6]);
			BS.finding().enterState(states[7]);
			HAS.finding().enterState(states[8]);
			// FS.finding().enterState(states[9]);
			WF.finding().enterState(states[10]);
			DD.finding().enterState(states[11]);
			DN.finding().enterState(states[12]);
			DW.finding().enterState(states[13]);
			SL.finding().enterState(states[14]);
			SM.finding().enterState(states[15]);
			int maxId = findMaxId(FS.getBeliefs());
			String maxState = FS.getStateNamesArray()[maxId];
			String observeState = states[9];
			if (maxState.equalsIgnoreCase(observeState))
				sameNumber++;
			H.finding().clear();
			HA.finding().clear();
			BH.finding().clear();
			BB.finding().clear();
			HS.finding().clear();
			KS.finding().clear();
			HIS.finding().clear();
			BS.finding().clear();
			HAS.finding().clear();
			FS.finding().clear();
			WF.finding().clear();
			DD.finding().clear();
			DN.finding().clear();
			DW.finding().clear();
			SL.finding().clear();
			SM.finding().clear();
		}
		System.out.println(FS.getStateNames());
		float[] beliefs = FS.getBeliefs();
		for (float belief : beliefs)
			System.out.print(belief + " ");
		System.out.println();
		System.out.println(sameNumber * 100 / testSet.size() + "%");
		net.finalize();
	}

	private static int findMaxId(float[] beliefs) {
		int result = 0;
		float max = -1;
		for (int i = 0; i < beliefs.length; i++)
			if (beliefs[i] > max) {
				max = beliefs[i];
				result = i;
			}
		return result;
	}

}
