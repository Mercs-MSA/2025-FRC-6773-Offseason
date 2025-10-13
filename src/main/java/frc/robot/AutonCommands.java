package frc.robot;

import java.util.Optional;

import javax.tools.StandardJavaFileManager.PathFactory;

import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.path.PathPlannerPath;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.Drive.DriveState;

public class AutonCommands {
    
    public Drive robotDrive;

    public AutonCommands(Drive robotDrive/*, Elevator elevator, Intake intake*/) {
        this.robotDrive = robotDrive;
    }

    public Command followChoreoPath(String pathName) {
        PathPlannerPath path = getTraj(pathName).get();
        path.getIdealTrajectory(Drive.robotConfig);
        double totalTimeSeconds = path.getIdealTrajectory(Drive.robotConfig).get().getTotalTimeSeconds();
        return 
            robotDrive.setDriveStateCommand(DriveState.AUTON).andThen(
                robotDrive.customFollowPathCommand(path).withTimeout(totalTimeSeconds), 
                robotDrive.setDriveStateCommand(DriveState.STOP));
    }

    public Command alignToReef() {
        return robotDrive.setDriveStateCommand(DriveState.DRIVE_TO_CORAL).andThen(
            robotDrive.waitUnitllAutoAlignFinishes());
    }

    
    // public Command alignToReef(boolean left) {
    //     PathPlannerPath path = new 
    // }

    public Command followChoreoPath(String pathName, PPHolonomicDriveController PID) {
        PathPlannerPath path = getTraj(pathName).get();
        path.getIdealTrajectory(Drive.robotConfig);
        double totalTimeSeconds = path.getIdealTrajectory(Drive.robotConfig).get().getTotalTimeSeconds();
        return 
            robotDrive.setDriveStateCommand(DriveState.AUTON).andThen(
                robotDrive.customFollowPathCommand(path, PID).withTimeout(totalTimeSeconds), 
                robotDrive.setDriveStateCommand(DriveState.STOP));
    }

    public Optional<PathPlannerPath> getTraj(String pathName) {
        try {
            return Optional.of(PathPlannerPath.fromChoreoTrajectory(pathName));
        } catch(Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
