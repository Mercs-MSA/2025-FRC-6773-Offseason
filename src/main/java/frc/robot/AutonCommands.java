package frc.robot;

import java.util.Optional;

import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.path.PathPlannerPath;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.Drive.DriveState;
import frc.robot.subsystems.drive.controllers.GoalPoseChooser;
import frc.robot.subsystems.drive.controllers.GoalPoseChooser.SIDE;
import frc.robot.subsystems.elevator.Elevator;
import frc.robot.subsystems.elevator.Elevator.ElevatorState;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.manipulator.Manipulator;

public class AutonCommands {
    
    public Drive kRobotDrive;
    public Intake kIntake;
    public Manipulator kManipulator;
    public Elevator kElevator;

    // public TeleopCommands(Elevator elevator, Intake intake, Manipulator manipulator, CommandXboxController controller) {
    //     kElevator = elevator;
    //     kIntake = intake;
    //     kManipulator = manipulator;
    //     kController = controller;
    //     // kClimb = climb;
    // }


    public AutonCommands(Drive robotDrive, Elevator elevator, Intake intake, Manipulator manipulator) {
        this.kRobotDrive = robotDrive;
        this.kIntake = intake;
        this.kElevator = elevator;
        this.kManipulator = manipulator;
        // this.
    }

    public Command followChoreoPath(String pathName) {
        PathPlannerPath path = getTraj(pathName).get();
        path.getIdealTrajectory(Drive.robotConfig);
        double totalTimeSeconds = path.getIdealTrajectory(Drive.robotConfig).get().getTotalTimeSeconds();
        return 
            kRobotDrive.setDriveStateCommand(DriveState.AUTON).andThen(
                kRobotDrive.customFollowPathCommand(path).withTimeout(totalTimeSeconds), 
                kRobotDrive.setDriveStateCommand(DriveState.STOP));
    }

    public Command followChoreoPath(String pathName, PPHolonomicDriveController PID) {
        PathPlannerPath path = getTraj(pathName).get();
        path.getIdealTrajectory(Drive.robotConfig);
        double totalTimeSeconds = path.getIdealTrajectory(Drive.robotConfig).get().getTotalTimeSeconds();
        return 
            kRobotDrive.setDriveStateCommand(DriveState.AUTON).andThen(
                kRobotDrive.customFollowPathCommand(path, PID).withTimeout(totalTimeSeconds), 
                kRobotDrive.setDriveStateCommand(DriveState.STOP));
    }

    public Optional<PathPlannerPath> getTraj(String pathName) {
        try {
            return Optional.of(PathPlannerPath.fromChoreoTrajectory(pathName));
        } catch(Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Command elevatorUpCommand(){
        return Commands.runOnce(() -> {
            if (kManipulator.getCoralDetected() || kElevator.getCurrentState() == ElevatorState.STOW) {
                kElevator.setElevatorGoalWithState();
            }
        });
    }

    public Command setElevatorStateCommand(ElevatorState state) {
        return Commands.runOnce(() -> kElevator.setElevatorState(state));
    }

    public Command elevatorAutoAlign(SIDE side)
    {
        return new ParallelCommandGroup(
            GoalPoseChooser.setSideCommand(side), 
            kRobotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_CORAL),
            elevatorUpCommand(),
            Commands.waitUntil(() -> kElevator.atGoal() && kRobotDrive.atGoal())
        );
    }

    public Command[] runAutonScoringSegment(ElevatorState elevatorLevel, String commandName) {
        return new Command[]{
            setElevatorStateCommand(elevatorLevel),
            followChoreoPath(commandName),
            Commands.parallel(
                elevatorUpCommand(),
                kRobotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_CORAL).onlyWhile(() -> !kRobotDrive.atGoal())),
            kManipulator.outtakeCommand()
        };
    }

    public Command[] runAutonIntakeSegment(String commandName) {
        return new Command[]{
            Commands.sequence(
                Commands.runOnce(() -> kManipulator.intake()), 
                followChoreoPath(commandName),
                kRobotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_INTAKE).onlyWhile(() -> !kRobotDrive.atGoal()))
        };
    }
}
