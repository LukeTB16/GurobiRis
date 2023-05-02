import gurobi.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
public class Main {
    public static void main(String[] args) {
        try {
            // Read input data from the text file
            String fileName = "input.txt"; // Replace with the path to your input file

            int n, m;
            int[][] d_ij;
            int[] r_i, s_j, alfa_j;
            double c;
            int k;

            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                n = Integer.parseInt(br.readLine().split(" ")[1]);
                m = Integer.parseInt(br.readLine().split(" ")[1]);

                br.readLine(); // Skip the "matrice d_ij" line
                d_ij = new int[m][n];
                for (int j = 0; j < m; j++) {
                    String[] line = br.readLine().split(" ");
                    for (int i = 0; i < n; i++) {
                        d_ij[j][i] = Integer.parseInt(line[i]);
                    }
                }

                br.readLine(); // Skip the "r_i" line
                r_i = Arrays.stream(br.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();

                br.readLine(); // Skip the "s_j" line
                s_j = Arrays.stream(br.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();

                c = Double.parseDouble(br.readLine().split(" ")[1]);
                k = Integer.parseInt(br.readLine().split(" ")[1]);

                br.readLine(); // Skip the "alfa_j" line
                alfa_j = Arrays.stream(br.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            } catch (IOException e) {
                System.out.println("Error reading the input file: " + e.getMessage());
                return;
            }

            // Create a Gurobi model
            GRBEnv env = new GRBEnv();
            GRBModel model = new GRBModel(env);

            // Define the decision variables
            // x_ij: amount of coffee transported from warehouse j to customer i
            GRBVar[][] x = new GRBVar[n][m];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    x[i][j] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x_" + i + "_" + j);
                }
            }

            // Add the constraints
            // Demand constraints
            for (int i = 0; i < n; i++) {
                GRBLinExpr demandExpr = new GRBLinExpr();
                for (int j = 0; j < m; j++) {
                    demandExpr.addTerm(1, x[i][j]);
                }
                model.addConstr(demandExpr, GRB.EQUAL, r_i[i], "demand_" + i);
            }

            // Capacity constraints
            for (int j = 0; j < m; j++) {
                GRBLinExpr capacityExpr = new GRBLinExpr();
                for (int i = 0; i < n; i++) {
                    capacityExpr.addTerm(1, x[i][j]);
                }
                model.addConstr(capacityExpr, GRB.LESS_EQUAL, s_j[j], "capacity_" + j);
            }

            // Distance constraints
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (d_ij[j][i] > k) {
                        model.addConstr(x[i][j], GRB.EQUAL, 0, "distance_" + i + "_" + j);
                    }
                }
            }

            // Set the objective function
            GRBLinExpr objExpr = new GRBLinExpr();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    objExpr.addTerm(c * d_ij[j][i], x[i][j]);
                }
            }
            model.setObjective(objExpr, GRB.MINIMIZE);

            // Risoluzione del modello
            model.optimize();

            if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
                double objVal = model.get(GRB.DoubleAttr.ObjVal);
                System.out.println("Valore ottimo della funzione obiettivo: " + objVal);

                // Risoluzione
                model.optimize();

                // Verifica se la soluzione è degenerata
                if (model.get(GRB.IntAttr.Status) == GRB.OPTIMAL) {
                    if (model.get(GRB.IntAttr.SolCount) > 1) {
                        System.out.println("La soluzione è degenere.");
                    } else {
                        System.out.println("La soluzione non è degenere.");
                    }
                }
                // Verifica se la soluzione è multipla
                int solCount = model.get(GRB.IntAttr.SolCount);
                if (solCount > 1) {
                    System.out.println("La soluzione è multipla.");
                } else {
                    System.out.println("La soluzione non è multipla.");
                }

            }


            // Ottenimento del valore ottimo delle variabili e della funzione obiettivo
            if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
                double[] soluzione = model.get(GRB.DoubleAttr.X, model.getVars());
                System.out.println("Valore ottimo delle variabili:");
                for (int i = 0; i < soluzione.length; i++) {
                    System.out.println("Variabile " + i + ": " + soluzione[i]);
                }
            }


            // Dispose the model and environment
            model.dispose();
            env.dispose();

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }
}