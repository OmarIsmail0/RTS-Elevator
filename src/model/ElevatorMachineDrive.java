package model;

import esper.Config;

import events.DoorSensorReading;
import events.ElevatorStateReading;

import javax.swing.*;
import java.awt.*;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElevatorMachineDrive extends TimerTask {

    int[] FloorY = {450, 340, 240};

    private final ElevatorCar elevator;
    private final ElevatorRequestController request;

    public ElevatorMachineDrive(ElevatorCar elevator, ElevatorRequestController request) {

        this.elevator = elevator;
        this.request = request;

        if (elevator.getCurrentFloor() == request.getRequestedFloor() || elevator.getIsMoving()) {
            try {
                this.cancel();
            } catch (Throwable ex) {
                Logger.getLogger(ElevatorMachineDrive.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Config.sendEvent(new DoorSensorReading(false));
        }
    }

    public int getCurrentFloor(int Y) {

        if (Y >= FloorY[0] - 1) {
            return 1;
        } else if (Y < FloorY[0] && Y >= FloorY[1]) {
            return 2;
        } else if (Y < FloorY[1] && Y >= FloorY[2]) {
            return 3;
        } else {
            return 0;
        }
    }

    @Override
    public void run() {
        if (elevator.isEmergencyTrigger()){
            this.cancel();
            return;
        }
        int FloorIdx = request.getRequestedFloor() - 1;
        JPanel Elevator = elevator.getGui().getCarPnel();
        String str ="";
        if (Math.abs(Elevator.getLocation().y - FloorY[FloorIdx]) > 0) {  // checks if the elevator isMoving
            if (Elevator.getLocation().y > FloorY[FloorIdx]) {
                Elevator.setLocation(Elevator.getLocation().x, Elevator.getLocation().y - 1);
                Config.sendEvent(new ElevatorStateReading(true, Elevator.getLocation().y, getCurrentFloor(Elevator.getLocation().y)));
                elevator.manageDoor("Closed");
                elevator.getGui().getWeightInput().setEnabled(false);
                elevator.getGui().getEmergencyStopBtn().setEnabled(true);
                elevator.getGui().getDoorBtn().setEnabled(false);
                elevator.getGui().getLightPanel().setBackground(Color.GREEN);
                int idx = getCurrentFloor(Elevator.getLocation().y);
                str = String.valueOf(idx);
                elevator.getGui().getFloorNo().setText(str);
            } else {
                Elevator.setLocation(Elevator.getLocation().x, Elevator.getLocation().y + 1);
                Config.sendEvent(new ElevatorStateReading(true, Elevator.getLocation().y, getCurrentFloor(Elevator.getLocation().y)));
                elevator.manageDoor("Closed");
                elevator.getGui().getWeightInput().setEnabled(false);
                elevator.getGui().getEmergencyStopBtn().setEnabled(true);
                elevator.getGui().getDoorBtn().setEnabled(false);
                elevator.getGui().getLightPanel().setBackground(Color.GREEN);
                int idx = getCurrentFloor(Elevator.getLocation().y);
                str = String.valueOf(idx);
                elevator.getGui().getFloorNo().setText(str);
            }
        } else {
            Config.sendEvent(new ElevatorStateReading(false, Elevator.getLocation().y, getCurrentFloor(Elevator.getLocation().y)));
            Config.sendEvent(new DoorSensorReading(true));
            request.getClickedBtn().setBackground(Color.PINK);
            elevator.manageDoor("Open");
            elevator.getGui().getWeightInput().setEnabled(true);
            elevator.getGui().getEmergencyStopBtn().setEnabled(false);
            elevator.getGui().getDoorBtn().setEnabled(true);
            elevator.getGui().getCloseDoorBtn().setEnabled(true);
            str = String.valueOf(FloorIdx+1);
            elevator.getGui().getFloorNo().setText(str);
            for (Component c : elevator.getGui().getCallPanl().getComponents()) {
                if (c instanceof JButton) {
                    if (c.getName().compareToIgnoreCase(request.getClickedBtn().getName()) == 0) {
                        c.setBackground(Color.PINK);
                        elevator.manageDoor("Open");
                        elevator.getGui().getLightPanel().setBackground(Color.GREEN);
                        elevator.getGui().getWeightInput().setEnabled(true);
                        elevator.getGui().getEmergencyStopBtn().setEnabled(false);
                        elevator.getGui().getDoorBtn().setEnabled(true);
                        elevator.getGui().getCloseDoorBtn().setEnabled(true);
                    }
                }
            }
            for (Component c : elevator.getGui().getElevatorControllerPanel().getComponents()) {
                if (c instanceof JButton) {
                    try {
                        if (((JButton) c).getName().compareToIgnoreCase("Floor" + request.getRequestedFloor() + "btn") == 0) {
                            c.setBackground(Color.PINK);
                            break;
                        }
                    } catch (Exception e) {

                    }
                }
            }
            this.cancel();
        }
    }
    
}
