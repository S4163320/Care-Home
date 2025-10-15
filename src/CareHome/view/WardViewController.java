package CareHome.view;

import CareHome.Model.Gender;
import CareHome.Model.Location.Bed;
import CareHome.Model.Person.Patient;
import CareHome.dao.BedDAO;
import CareHome.dao.BedDAOImpl;
import CareHome.dao.PatientDAO;
import CareHome.dao.PatientDAOImpl;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.stream.Collectors;

public class WardViewController {

    @FXML private GridPane ward1Grid;
    @FXML private GridPane ward2Grid;

    private BedDAO bedDAO = new BedDAOImpl();
    private PatientDAO patientDAO = new PatientDAOImpl();

    // Entry point for the view: clears and repaints the wards initially
    @FXML
    public void initialize() {
        refresh();
    }

    // Rebuilds both ward grids: queries beds/patients, maps occupancy, and renders rooms/beds
    public void refresh() {
        try {
            ward1Grid.getChildren().clear();
            ward2Grid.getChildren().clear();

            List<Bed> allBeds = bedDAO.getAllBeds();
            List<Patient> allPatients = patientDAO.getAllPatients();

            // Fixed: Create patient bed map properly
            Map<String, Patient> patientBedMap = new HashMap<>();
            for (Patient patient : allPatients) {
                try {
                    String bedId = patientDAO.getPatientBed(patient.getId());
                    if (bedId != null && !bedId.isEmpty()) {
                        patientBedMap.put(bedId, patient);
                    }
                } catch (Exception e) {
                    System.err.println("Error getting bed for patient " + patient.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Total patients: " + allPatients.size());
            System.out.println("Patients with beds: " + patientBedMap.size());
            System.out.println("Patient-Bed mapping: " + patientBedMap);

            // Group beds by ward and room
            Map<String, Map<Integer, List<Bed>>> wards = allBeds.stream()
                    .filter(bed -> bed.getWardId() != null)
                    .collect(Collectors.groupingBy(
                            Bed::getWardId,
                            Collectors.groupingBy(
                                    Bed::getRoomNumber,
                                    TreeMap::new,
                                    Collectors.toList()
                            )
                    ));

            populateWardGrid(ward1Grid, wards.get("W1"), patientBedMap, "W1");
            populateWardGrid(ward2Grid, wards.get("W2"), patientBedMap, "W2");

        } catch (Exception e) {
            System.err.println("Error in refresh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Populates a ward grid with room panes and their bed panes laid out in rows/cols
    private void populateWardGrid(GridPane wardGrid, Map<Integer, List<Bed>> rooms, Map<String, Patient> patientBedMap, String wardId) {
        if (rooms == null) {
            System.out.println("No rooms found for ward: " + wardId);
            return;
        }

        // Style the main ward grid
        wardGrid.setStyle("-fx-border-color: #4A90E2; -fx-border-width: 3; -fx-padding: 20; -fx-background-color: #F5F5F5;");
        wardGrid.setHgap(15);
        wardGrid.setVgap(15);

        // Sort rooms by room number to ensure consistent ordering
        List<Integer> sortedRoomNumbers = new ArrayList<>(rooms.keySet());
        Collections.sort(sortedRoomNumbers);

        System.out.println("Ward " + wardId + " has rooms: " + sortedRoomNumbers);

        // Process each room and place in 3x2 grid
        int roomIndex = 0;
        for (Integer roomNumber : sortedRoomNumbers) {
            List<Bed> bedsInRoom = rooms.get(roomNumber);

            // Sort beds within room for consistent ordering
            bedsInRoom.sort(Comparator.comparing(Bed::getBedId));

            System.out.println("Room " + roomNumber + " has " + bedsInRoom.size() + " beds: " +
                    bedsInRoom.stream().map(Bed::getBedId).collect(Collectors.toList()));

            // Create room container
            GridPane roomPane = new GridPane();
            roomPane.setHgap(8);
            roomPane.setVgap(8);
            roomPane.setStyle("-fx-border-color: #2C3E50; -fx-border-width: 2; -fx-padding: 12; -fx-background-color: white;");
            roomPane.setAlignment(Pos.TOP_LEFT);

            // Set consistent room size
            roomPane.setPrefWidth(150);
            roomPane.setPrefHeight(150);

            // Add beds to room in 2x2 grid
            int bedIndex = 0;
            for (Bed bed : bedsInRoom) {
                int col = bedIndex % 2;
                int row = bedIndex / 2;

                StackPane bedPane = createBedPane(bed, patientBedMap);
                roomPane.add(bedPane, col, row);
                bedIndex++;
            }

            // Position room in ward grid (3 rows, 2 columns)
            // Room 1 -> (0,0), Room 2 -> (0,1)
            // Room 3 -> (1,0), Room 4 -> (1,1)
            // Room 5 -> (2,0), Room 6 -> (2,1)
            int wardRow = roomIndex / 2;
            int wardCol = roomIndex % 2;

            wardGrid.add(roomPane, wardCol, wardRow);
            roomIndex++;
        }
    }

    // Creates a stylized bed pane with gender-based color fill and tooltip (or empty bed style)
    private StackPane createBedPane(Bed bed, Map<String, Patient> patientBedMap) {
        // Create bed rectangle
        Rectangle bedShape = new Rectangle(70, 70);
        bedShape.setArcWidth(5);
        bedShape.setArcHeight(5);
        bedShape.setStroke(Color.BLACK);
        bedShape.setStrokeWidth(1.5);

        Text bedIdText = new Text(bed.getBedId());
        bedIdText.setFont(Font.font("System", FontWeight.NORMAL, 10));

        StackPane bedPane = new StackPane(bedShape, bedIdText);

        Patient patient = patientBedMap.get(bed.getBedId());

        if (patient != null) {
            // Color based on gender
            Color fillColor = patient.getGender() == Gender.MALE ? Color.LIGHTBLUE : Color.LIGHTPINK;
            bedShape.setFill(fillColor);

            System.out.println("Bed " + bed.getBedId() + " has patient: " + patient.getName() +
                    " (Gender: " + patient.getGender() + ", Color: " + fillColor + ")");

            Tooltip.install(bedPane, new Tooltip("Patient: " + patient.getName() + "\nID: " + patient.getPatientId()));
        } else {
            bedShape.setFill(Color.WHITE);
            System.out.println("Bed " + bed.getBedId() + " is empty");
            Tooltip.install(bedPane, new Tooltip("Bed: " + bed.getBedId() + " (Empty)"));
        }

        return bedPane;
    }
}