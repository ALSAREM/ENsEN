package ensen.entities;

public class Tensor {
	public double[][] U0;
	public double[][] U1;
	public double[][] U2;
	public double[] lamda;

	public Tensor(String serializedData) {
		String[] lines = serializedData.split(System.getProperty("line.separator"));
		boolean currentIsU0 = false;
		boolean currentIsU1 = false;
		boolean currentIsU2 = false;
		boolean currentIsLamda = false;
		int u0 = 0;
		int u1 = 0;
		int u2 = 0;
		int i = 0;
		while (i < lines.length) {
			String line = lines[i];
			if (line.contains("U0")) {
				U0 = new double[Integer.parseInt(lines[++i])][Integer.parseInt(lines[++i])];
				currentIsU0 = true;
			} else if (line.contains("U1")) {
				U1 = new double[Integer.parseInt(lines[++i])][Integer.parseInt(lines[++i])];
				currentIsU1 = true;
			} else if (line.contains("U2")) {
				U2 = new double[Integer.parseInt(lines[++i])][Integer.parseInt(lines[++i])];
				currentIsU2 = true;
			} else if (line.contains("U3")) {
				lamda = new double[Integer.parseInt(lines[++i])];
				currentIsLamda = true;
			} else {
				line.replace("[", "").replace("]", "");
				String[] values = line.split(" ");
				if (currentIsU0) {
					for (int j = 0; j < values.length; j++) {
						U0[u0][j] = Double.parseDouble(values[j]);
					}
				} else if (currentIsU1) {
					for (int j = 0; j < values.length; j++) {
						U1[u1][j] = Double.parseDouble(values[j]);
					}

				} else if (currentIsU2) {
					for (int j = 0; j < values.length; j++) {
						U2[u2][j] = Double.parseDouble(values[j]);
					}

				} else if (currentIsLamda) {
					for (int j = 0; j < values.length; j++) {
						lamda[j] = Double.parseDouble(values[j]);
					}
				}
			}
			i++;
		}
	}
}
