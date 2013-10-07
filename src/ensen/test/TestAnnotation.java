package ensen.test;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.Text;

import ensen.control.DBpediaSpotlightClient;
import ensen.entities.EnsenDBpediaResource;

public class TestAnnotation {

	private JFrame frmEnsen;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestAnnotation window = new TestAnnotation();
					window.frmEnsen.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TestAnnotation() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	JTextArea textArea_1;
	JTextArea txtrGeographyOfSyria;
	JButton btnGo;
	final static int interval = 100;
	Timer timer;
	JProgressBar progressBar;
	String resString = "";
	String inString = "";
	boolean Finished = false;

	class DBSL implements Runnable {
		public void run() {
			progressBar.setIndeterminate(true);
			inString = txtrGeographyOfSyria.getText();
			textArea_1.setText("");
			progressBar.setValue(0);
			btnGo.setEnabled(false);
			DBpediaSpotlightClient c = new DBpediaSpotlightClient();
			try {
				List<EnsenDBpediaResource> responses = c.ensenExtract(new Text(inString));
				resString = getPrintableResList(inString, responses);
			} catch (AnnotationException e) {
				e.printStackTrace();
			}
			System.out.print("ok\n");
			progressBar.setIndeterminate(false);
			progressBar.setValue(0);
			textArea_1.setText(resString);
			btnGo.setEnabled(true);
		}

	}

	String getPrintableResList(String in, List<EnsenDBpediaResource> responses) {
		String res = "";
		//int currOffset=0;
		for (int i = 0; i < responses.size(); i++) {
			DBpediaResource R = responses.get(i);

			res += "\n " + i + "- " + R.toString() + "(" + R.getFullUri() + ")";

		}

		return res;
	}

	private void initialize() {

		frmEnsen = new JFrame();
		frmEnsen.setTitle("ENsEN");
		frmEnsen.setBounds(100, 100, 899, 597);
		frmEnsen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 0, 702, 151);
		frmEnsen.getContentPane().add(scrollPane);
		txtrGeographyOfSyria = new JTextArea();
		txtrGeographyOfSyria
				.setText("Geography of Syria\r\n\r\nSyria lies between latitudes 32\u00B0 and 38\u00B0 N, and longitudes 35\u00B0 and 43\u00B0 E. It consists mostly of arid plateau, although the northwest part of the country bordering the Mediterranean is fairly green. The Northeast of the country \"Al Jazira\" and the South \"Hawran\" are important agricultural areas. The Euphrates, Syria's most important river, crosses the country in the east. It is considered to be one of the fifteen states that comprise the so-called \"Cradle of civilization\".\r\nThe climate in Syria is dry and hot, and winters are mild. Because of the country's elevation, snowfall does occasionally occur during winter. Petroleum in commercial quantities was first discovered in the northeast in 1956. The most important oil fields are those of Suwaydiyah, Qaratshui, Rumayian, and Tayyem, near Dayr az\u2013Zawr. The fields are a natural extension of the Iraqi fields of Mosul and Kirkuk. Petroleum became Syria's leading natural resource and chief export after 1974. Natural gas was discovered at the field of Jbessa in 1940.");
		scrollPane.setViewportView(txtrGeographyOfSyria);
		txtrGeographyOfSyria.setWrapStyleWord(true);
		txtrGeographyOfSyria.setLineWrap(true);
		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setBounds(10, 162, 702, 14);
		frmEnsen.getContentPane().add(progressBar);

		btnGo = new JButton("Go");
		btnGo.setBounds(720, 11, 153, 140);
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Thread t = new Thread(new DBSL());
				System.out.print("Start Thread\n");
				t.start();

			}
		});

		timer = new Timer(interval, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				progressBar.setValue(progressBar.getValue() + 1);
			}
		});

		frmEnsen.getContentPane().setLayout(null);
		frmEnsen.getContentPane().add(btnGo);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 193, 863, 285);
		frmEnsen.getContentPane().add(scrollPane_1);

		textArea_1 = new JTextArea();
		scrollPane_1.setViewportView(textArea_1);
		textArea_1.setWrapStyleWord(true);
		textArea_1.setLineWrap(true);

	}
}
