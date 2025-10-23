package frc.robot;

import java.util.Optional;

import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.path.PathPlannerPath;

import edu.wpi.first.units.TimeUnit;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.Drive.DriveState;
import frc.robot.subsystems.drive.controllers.GoalPoseChooser;
import frc.robot.subsystems.drive.controllers.GoalPoseChooser.SIDE;
import frc.robot.subsystems.elevator.Elevator;
import frc.robot.subsystems.elevator.Elevator.ElevatorGoal;
import frc.robot.subsystems.elevator.Elevator.ElevatorState;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.Intake.IntakePivotGoal;
import frc.robot.subsystems.manipulator.Manipulator;

public class AutonCommands {
    
    public Drive kRobotDrive;
    public Intake kIntake;
    public Manipulator kManipulator;
    public Elevator kElevator;

    private boolean stopRollers = false;
    private boolean stopPivot = false;


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
                kRobotDrive.customFollowPathCommand(path), 
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

    public Command elevatorDownCommand(){
        return Commands.runOnce(() -> {
            if (kElevator.getCurrentState() != ElevatorState.STOW) {
                kElevator.setElevatorState(ElevatorState.STOW);
                kElevator.setElevatorGoalWithState();
            }
        });
    }

    public Command setElevatorStateCommand(ElevatorState state) {
        return Commands.runOnce(() -> kElevator.setElevatorState(state));
    }

    public Command runRollerCommand() {
        return Commands.run(() -> {
            stopRollers = false;
            if (!kManipulator.getCoralDetected()) { kIntake.runRollers(); } else { kIntake.stopRollers(); };
        });
    }

    public Command stopRollerCommand() {
        return setStopRollerStateCommand(true).andThen(Commands.runOnce(() -> kIntake.stop(true, false)));
    }

    private Command setStopPivotStateCommand(boolean stopPivotState) {
        return Commands.runOnce(() -> stopPivot = stopPivotState);
    }

    private Command setStopRollerStateCommand(boolean stopRollerState) {
        return Commands.runOnce(() -> stopRollers = stopRollerState);
    }

    public Command runManipulatorRollersCommand() {
        return Commands.runOnce(() -> {
            kManipulator.intake();
        });
    }

    public Command stopManipulatorRollersCommand() {
        return Commands.runOnce(() -> {
            kManipulator.stop();
        });
    }

    public Command runIntake() {
        return Commands.parallel(

            runRollerCommand(),
            runManipulatorRollersCommand()
        );
    }

    public Command runPivotAndHoldCommand(IntakePivotGoal pivotGoal) {
        return Commands.startEnd(
            () -> {
                stopPivot = false;
                kIntake.setPivotGoal(pivotGoal);
            }, 
            () -> {
                stopPivot = false;
                kIntake.setPivotPosition(kIntake.getPivotPosition());
            }, 
            kIntake);
    }

    public Command substationIntakeCommand() {
        return Commands.parallel(
            runPivotAndHoldCommand(IntakePivotGoal.kStow),
            new InstantCommand(()-> kElevator.setGoal(ElevatorGoal.kStow)),
            runRollerCommand(),
            runManipulatorRollersCommand()
        );
    }

    public Command stowCommand() {
        // return runPivotAndHoldCommand(kManipulator.getCoralDetected() ? IntakePivotGoal.kStow : IntakePivotGoal.kTransfer);
        return Commands.parallel(runPivotAndHoldCommand(IntakePivotGoal.kStow), runRollerCommand());
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

    public Command[] runAutonScoringSegmentFirst(ElevatorState elevatorLevel, String commandName, SIDE side) {
        return new Command[]{
            setElevatorStateCommand(elevatorLevel),
            followChoreoPath(commandName),
            Commands.parallel(
                elevatorUpCommand(),
                GoalPoseChooser.setSideCommand(side)
                .andThen(kRobotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_CORAL)
                .onlyWhile(() -> !kRobotDrive.atGoal())
                .withDeadline(kRobotDrive.waitUnitllAutoAlignFinishes()))
                .andThen(kManipulator.outtakeCommand()
                .andThen(new WaitCommand(0.5)))),
        };
    }

    public Command[] runAutonScoringSegment(ElevatorState elevatorLevel, String commandName, SIDE side) {
        return new Command[]{
            setElevatorStateCommand(elevatorLevel),
            followChoreoPath(commandName),
            Commands.parallel(
                elevatorUpCommand(),
                //substationIntakeCommand().withTimeout(2),
                GoalPoseChooser.setSideCommand(side)
                .andThen(kRobotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_CORAL)
                .onlyWhile(() -> !kRobotDrive.atGoal())
                .withDeadline(kRobotDrive.waitUnitllAutoAlignFinishes()))
                .andThen(kManipulator.outtakeCommand()
                .andThen(new WaitCommand(0.5)))),
        };
    }

    public Command[] runAutonIntakeSegment(String commandName) {
        return new Command[]{
            Commands.sequence(
                elevatorDownCommand(),
                followChoreoPath(commandName),
                Commands.parallel(
                    substationIntakeCommand().withTimeout(1.5),
                    kRobotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_INTAKE)
                    .onlyWhile(() -> !kRobotDrive.atGoal())
                )
            )
        };
    }

    

}
