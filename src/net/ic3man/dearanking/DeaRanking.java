package net.ic3man.dearanking;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.opensourcedea.dea.*;


@SuppressWarnings("unused")
public class DeaRanking {

	static ArrayList<DMU> DMUs = new ArrayList<DMU>();

	static String[] VariableNames = new String[3];
	static VariableOrientation[] VariableOrientations = new VariableOrientation[3];
	static VariableType[] VariableTypes = new VariableType[3];
	static double[][] DataMatrix = new double[10][3];

	static DeaGUI dGUI;

	static double optiC1 = 0;
	static double optiC2 = 0;

	public static void main(String[] args) {
		dGUI = new DeaGUI();
	}

	static String[] dmuCodes() {
		ArrayList<String> names = new ArrayList<String>();
		for (DMU d : DMUs) {
			names.add(d.getCode());
		}
		return names.toArray(new String[names.size()]);
	}

	static String[] dmuNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (DMU d : DMUs) {
			names.add(d.getName());
		}
		return names.toArray(new String[names.size()]);
	}

	public static double randDouble(double low, double high) {
		Random r = new Random();
		double random = new Random().nextDouble();
		double result = low + (random * (high - low));
		return result;
	}

	public static void readXLSX(String filename) {
		// 0ΚΩΔΙΚΟΣ 1ΚΑΤΗΓΟΡΙΑ 2ΟΝΟΜΑΣΙΑΤΟΠΟΥ 3ΕΚΤΑΣΗ (ha) 4LON_DEG 5LON_MIN
		// 6LON_SEC 7LAT_DEG 8LAT_MIN 9LAT_SEC 10ALT_MEAN 11ALT_MAX 12ALT_MIN
		// 13Habitats (C1) 14SUM (C2)

		DMUs.clear();

		try {
			InputStream inp = new FileInputStream(filename);

			Workbook wb = WorkbookFactory.create(inp);

			Sheet sheet = wb.getSheetAt(0);
			boolean header = true;
			for (Row row : sheet) {
				if (!header) {
					try {
						String code = row.getCell(0, Row.RETURN_BLANK_AS_NULL)
								.getStringCellValue();
						String category = row.getCell(1,
								Row.RETURN_BLANK_AS_NULL).getStringCellValue();
						String name = row.getCell(2, Row.RETURN_BLANK_AS_NULL)
								.getStringCellValue();
						double area = row.getCell(3, Row.RETURN_BLANK_AS_NULL)
								.getNumericCellValue();

						double lond = row.getCell(4, Row.RETURN_BLANK_AS_NULL)
								.getNumericCellValue();
						double lonm = row.getCell(5, Row.RETURN_BLANK_AS_NULL)
								.getNumericCellValue();
						double lons = row.getCell(6, Row.RETURN_BLANK_AS_NULL)
								.getNumericCellValue();
						double lon = lond + (lonm / 60.) + (lons / 3600.);

						double latd = row.getCell(7, Row.RETURN_BLANK_AS_NULL)
								.getNumericCellValue();
						double latm = row.getCell(8, Row.RETURN_BLANK_AS_NULL)
								.getNumericCellValue();
						double lats = row.getCell(9, Row.RETURN_BLANK_AS_NULL)
								.getNumericCellValue();
						double lat = latd + (latm / 60.) + (lats / 3600.);

						int c1 = (int) row
								.getCell(13, Row.RETURN_BLANK_AS_NULL)
								.getNumericCellValue();
						int c2 = (int) row
								.getCell(14, Row.RETURN_BLANK_AS_NULL)
								.getNumericCellValue();
						
						DMU dmu = new DMU(code, category, name, area, lat, lon,
								c1, c2);
						DMUs.add(dmu);


					} catch (NullPointerException e) {
					}
				} else {
					header = false;
				}
			}

			// Create matrix
			createData();
			// Initiate DEA
			init();

		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void init() {

		// Create a DEAProblem and specify number of DMUs (7) and number of
		// variables (3).
		DEAProblem tester = new DEAProblem(DMUs.size(), 3);

		// Set the DEA Problem Model Type (CCR Input Oriented).
		tester.setModelType(ModelType.CCR_O);

		// Set the DEA Problem DMU Names where testDMUName is a double[].
		tester.setDMUNames(dmuCodes());

		// Set the DEA Problem Variable Names where testVariableName is a
		// String[].
		tester.setVariableNames(VariableNames);

		// Set the DEA Problem Variable Orientation where
		// testVariableOrientation is a VariableOrientation[].
		tester.setVariableOrientations(VariableOrientations);

		// Set the DEA Problem Variable Types where testVariableTypes is a
		// VariableType[].
		tester.setVariableTypes(VariableTypes);

		/*
		 * Set the DEA Problem Data Matrix where testDataMatrix is a double[]
		 * []. Each row of the Matrix corresponds to the DMU in the DMUNames
		 * array. Each Column of the Matrix corresponds to the Variable in the
		 * Variables Arrays.
		 */
		tester.setDataMatrix(DataMatrix);

		try {
			// Solve the DEA Problem
			tester.solve();

			// Get the solution Objectives
			double[] objectives = tester.getObjectives();

			/* Get the solution Reference Set. */
			ArrayList<NonZeroLambda>[] referenceSets = new ArrayList[DMUs
					.size()];
			referenceSets = tester.getReferenceSet();

			/*
			 * Get the solution Slacks. The first array corresponds to the DMUs.
			 * The second nested array corresponds to the Slack values.
			 */
			double[][] slacks = tester.getSlacks();

			/*
			 * Get the solution Projections. The first array corresponds to the
			 * DMUs. The second nested array corresponds to the Projection
			 * values.
			 */
			double[][] projections = tester.getProjections();

			/*
			 * Get the solution Weights. The first array corresponds to the
			 * DMUs. The second nested array corresponds to the Weight values.
			 */
			double[][] weights = tester.getWeight();

			/*
			 * Get the DMU ranks. The boolean confirms that the Highest DMU
			 * score is ranked first. The STANDARD ranking type confirms that
			 * the ranking is standard. This means that if they are two DMUs
			 * with an efficiency score of 1 both will be ranked first. However,
			 * the following DMU will only be ranked 3rd as they are two DMUs
			 * which score better than it. Conversely, a DENSE RankingType will
			 * have given the following (3rd) DMU the ranking of second. The
			 * precision is the int value (between 0 and 16) used to round the
			 * score values before ranking the objectives.
			 */
			int[] ranks = tester.getRanks(true, RankingType.STANDARD, 5);

			// Print Weights (e)
			// for (int i = 0; i < DMUs.size(); i++) {
			// System.out.println("DMU "
			// + (i + 1)
			// + " - "
			// + Double.valueOf(new DecimalFormat("#.###")
			// .format(weights[i][2])));
			// }
			double[] TE = new double[DMUs.size()];

			// Calculate technical efficiency
			int TE1 = 0;
			for (int i = 0; i < DMUs.size(); i++) {
				DMUs.get(i).setTE(
						1 / Double.valueOf(new DecimalFormat("#.###")
								.format(weights[i][2])));
				if (DMUs.get(i).getTE() == 1.0)
					TE1++;
			}

			switch (TE1) {
			case 0:
				break;
			case 1:
				case1();
				break;
			case 2:
				case2();
				break;
			case 3:
				case3();
				break;
			default:
				break;
			}
			Collections.sort(DMUs, new MyDMUComparator());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void case1() {
		// Find Y1c Y2c
		double Y1c = 0;
		double Y2c = 0;
		for (int i = 0; i < DMUs.size(); i++) {
			if (DMUs.get(i).getTE() == 1.0) {
				Y1c = DataMatrix[i][0];
				Y2c = DataMatrix[i][1];
			}
		}

		optiC1 = Y1c;
		optiC2 = Y1c;

		// Calculate D1
		for (int i = 0; i < DMUs.size(); i++) {
			DMUs.get(i).setD1(
					Math.sqrt(Math.pow(DataMatrix[i][0] - Y1c, 2)
							+ Math.pow(DataMatrix[i][1] - Y2c, 2)));
		}
	}

	static double weightX = 0.5;
	static double weightY = 0.5;

	private static void case2() {

		dGUI.readWeights();

		// Find Y1c Y2c
		double YY1 = 0;
		double YY2 = 0;
		boolean firstFound = false;
		for (int i = 0; i < DMUs.size(); i++) {
			if (!firstFound && DMUs.get(i).getTE() == 1.0) {
				YY1 += DataMatrix[i][0] * weightX;
				YY2 += DataMatrix[i][1] * weightY;
				firstFound = true;
			} else if (DMUs.get(i).getTE() == 1.0) {
				YY1 += DataMatrix[i][0] * weightX;
				YY2 += DataMatrix[i][1] * weightY;
			}
		}

		optiC1 = YY1;
		optiC2 = YY2;

		// Calculate D1
		for (int i = 0; i < DMUs.size(); i++) {
			DMUs.get(i).setD1(
					Math.sqrt(Math.pow(DataMatrix[i][0] - YY1, 2)
							+ Math.pow(DataMatrix[i][1] - YY2, 2)));
		}

	}

	private static void case3() {
		// Find Points
		ArrayList<Double> xx = new ArrayList<Double>();
		ArrayList<Double> yy = new ArrayList<Double>();
		for (int i = 0; i < DMUs.size(); i++) {
			if (DMUs.get(i).getTE() == 1.0) {
				xx.add(DataMatrix[i][0]);
				yy.add(DataMatrix[i][1]);
			}
		}

		PolynomialFunctionLagrangeForm polyLagrange = new PolynomialFunctionLagrangeForm(
				ArrayUtils.toPrimitive(xx.toArray(new Double[xx.size()])),
				ArrayUtils.toPrimitive(yy.toArray(new Double[yy.size()])));
		double[] polyCoeff = polyLagrange.getCoefficients();
		PolynomialFunction poly = new PolynomialFunction(polyCoeff);
		// Print function
		// System.out.println(poly.toString());

		PolynomialFunction polyD1 = poly.polynomialDerivative();
		// Print derivative
		// System.out.println(polyD1.toString());

		LaguerreSolver lSolver = new LaguerreSolver();
		Complex[] polyRoots = lSolver.solveAllComplex(polyD1.getCoefficients(),
				0);

		PolynomialFunction polyD2 = polyD1.polynomialDerivative();

		double maxRoot = 0;
		for (int i = 0; i < polyRoots.length; i++) {
			if (polyD2.value(polyRoots[i].getReal()) < 0
					&& polyRoots[i].getReal() > maxRoot)
				maxRoot = polyRoots[i].getReal();
		}

		optiC1 = maxRoot;
		optiC2 = poly.value(maxRoot);

		// Calculate D1
		for (int i = 0; i < DMUs.size(); i++) {
			DMUs.get(i).setD1(
					Math.sqrt(Math.pow(DataMatrix[i][0] - maxRoot, 2)
							+ Math.pow(DataMatrix[i][1] - poly.value(maxRoot),
									2)));
		}
	}

	private static void createData() {

		DataMatrix = new double[DMUs.size()][3];

		// Set up the Variable Names
		VariableNames[0] = "Habitats (C1)";
		VariableNames[1] = "SUM (C2)";
		VariableNames[2] = "dummy";

		// Set up the variable types
		VariableOrientations[0] = VariableOrientation.OUTPUT;
		VariableOrientations[1] = VariableOrientation.OUTPUT;
		VariableOrientations[2] = VariableOrientation.INPUT;

		VariableTypes[0] = VariableType.STANDARD;
		VariableTypes[1] = VariableType.STANDARD;
		VariableTypes[2] = VariableType.STANDARD;

		// Set up the Data Matrix
		int i = 0;
		for (DMU d : DMUs) {
			DataMatrix[i][0] = d.getC1();
			DataMatrix[i][1] = d.getC2();
			DataMatrix[i][2] = 1;
			i++;
		}

	}

}

class MyDMUComparator implements Comparator<DMU> {

	@Override
	public int compare(DMU o1, DMU o2) {
		return (o1.getD1() < o2.getD1() ? -1 : (o1.getD1() == o2.getD1() ? 0
				: 1));
	}

}