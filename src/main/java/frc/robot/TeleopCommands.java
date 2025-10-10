
package frc.robot;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

// import frc.robot.subsystems.elevator.Elevator;
// import frc.robot.subsystems.elevator.Elevator.RobotContainer.ElevatorGoal;
// import frc.robot.subsystems.intake.Intake;
// import frc.robot.subsystems.intake.Intake.Gamepiece;
// import frc.robot.subsystems.intake.Intake.RobotContainer.IntakePivotGoal;
// import frc.robot.subsystems.intake.Intake.RollerGoal;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.RobotContainer;
// import frc.robot.subsystems.climb.Climb;
// import frc.robot.subsystems.climb.Climb.ClimbVoltageGoal;
import frc.robot.utils.debugging.LoggedTunableNumber;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;



/**
 * <p> A commands factory for the teleoperated period. 
 * 
 * <p> Note that when creating a command it should just be that base command, with no 
 * decorators or "special logic" unless it is absolutely needed. Generally speaking, 
 * that logic should be handled by the caller.
 * 
 * <p> While it is not necessary to create a command that always has an action to run
 * when its end condition is invoked or is interrupted, it is generally preferred to have
 * one in the event the caller does not specify what the command should do if it is
 * interrupted
 */

 //TODO: rewrite logic, get rid of the two stop booleans
public class TeleopCommands {


    // private final Elevator kElevator;
    // private final Intake kIntake;
    // private final Climb kClimb;

    /** 
     * Internal state to decide whether or not to stop the rollers when the intake's 
     * stop method is invoked. When creating a command that requires the rollers, this
     * variable should be set to true when the rollers are running then set to false
     * when they should no longer run
     */
    private static boolean stopRollers = false;
    /** 
     * Internal state to decide whether or not to stop the algae picker when the intake's 
     * stop method is invoked. When creating a command that requires the algae picker, this
     * variable should be set to true when the algae picker is running then set to false
     * when it should no longer run
     */
    private static boolean stopPivot = false;

    public RobotContainer.TeleopState currentState = RobotContainer.TeleopState.BASE;


    /**
     * Creates a new TeleopCommands factory
     * 
     * @param elevator The elevator subsystem instance
     * @param intake The intake subsystem intance
     * @param climb The climb subsystem instance
     */
    public TeleopCommands(/*Intake intake*/) {
        // kElevator = elevator;
        // kIntake = intake;
        // kClimb = climb;
    }

    /**
     * Runs the algae picker pivot and then stops it as well as the rollers, this command should 
     * be decorated with an end condition specified by the caller
     * 
     * @param pivotGoal The algae picker pivot goal
     * @return The command to start the algae picker pivot and stop the entire intake
     */
    public static Command runPivotAndStopIntakeCommand(RobotContainer.IntakePivotGoal pivotGoal) {
        return Commands.startEnd(
            ()-> {
                stopPivot = false;
                stopRollers = false;
                // kIntake.setPivotGoal(pivotGoal);
            }, 
            () -> {
                stopPivot = true;
                stopRollers = true;
                // kIntake.stop(stopRollers, stopPivot);
            }
            // ,kIntake
            );
    }

    public static Command runPivotAndHoldCommand(RobotContainer.IntakePivotGoal pivotGoal) {
        return Commands.startEnd(
            () -> {
                stopPivot = false;
                // kIntake.setPivotGoal(pivotGoal);
            }, 
            () -> {
                stopPivot = false;
                // kIntake.setPivotPosition(kIntake.getPivotPosition());
            }
            // , kIntake
            );
    }

    public static Command runRollerCommand() {
        return Commands.runOnce(() -> {
            stopRollers = false;
            // kIntake.runRollers();
        });
    }

    public static Command runRollerCommandWhile() {
        return Commands.startEnd(
            () -> {
                stopRollers = false;
                // kIntake.runRollers();
            }, 
            () -> {
                stopRollers = true;
                // kIntake.stop(true, false);
            }
            // , kIntake
            );
    }

    public static Command toggleRollerCommand() {
        return Commands.runOnce(() -> {
            stopRollers = !stopRollers;
            if (stopRollers) {
                // kIntake.stop(true, false);
            } else {
                // kIntake.runRollers();
            }
        });
    }

    /**
     * Stops the intake rollers. This will also stop the pivot if the
     * stopPivot variable is set to true
     * 
     * @return The command to stop the rollers that runs once
     */
    // public static Command stopRollersCommand() {
    //     // Note that the state must be set via command and not in method since the method
    //     // only returns an instance of the command and does not run its internal logic
    //     return setStopRollersStateCommand(true)
    //         .andThen(
    //             Commands.runOnce(() -> kIntake.stop(stopRollers, stopPivot), kIntake));
    // }

    /**
     * Stops the intake pivot. This will also stop the rollers if the stopRollers
     * internal variable is set to true
     * 
     * @return The command to stop the pivot that runs once
     */
    public static Command stopPivotCommand() {
        // Note that the state must be set via command and not in method since the method
        // only returns an instance of the command and does not run its internal logic
        return setStopPivotStateCommand(true)
            .andThen(
                // Commands.runOnce(() -> kIntake.stop(stopRollers, stopPivot), kIntake)
                );
    }

    public static Command stopRollerCommand() {
        return setStopRollerStateCommand(true).andThen(
            // Commands.runOnce(() -> kIntake.stop(true, false))
            );
    }

    // public static Command stopRollersAndPivotCommand() {
    //     return setStopRollersStateCommand(true)
    //         .andThen(setStopPivotStateCommand(true)
    //             .andThen(
    //                 Commands.runOnce(() -> kIntake.stop(stopRollers, stopPivot), kIntake)));
    // }



    /**
     * Since changing the state requires a command to be scheduled and ran, this method
     * returns a command to change the pivot state
     * 
     * @param stopRollersState The desired state
     * @return The command to chagne the pivot state
     */
    public static Command setStopPivotStateCommand(boolean stopPivotState) {
        return Commands.runOnce(() -> stopPivot = stopPivotState);
    }

    public static Command setStopRollerStateCommand(boolean stopRollerState) {
        return Commands.runOnce(() -> stopRollers = stopRollerState);
    }


    public static Command elevatorToGoal(RobotContainer.ElevatorGoal kintake) {
        return Commands.runOnce(() -> System.out.println() /*kElevator.setGoal(goal)*/);
    }

    public static Command stopElevator() {
        return Commands.runOnce(() -> System.out.println() /*kElevator.stop()*/);
    }


    public SequentialCommandGroup toBase () {
        return new SequentialCommandGroup(new Command[]{
            runPivotAndHoldCommand(RobotContainer.IntakePivotGoal.kBase),
            elevatorToGoal(RobotContainer.ElevatorGoal.kIntake),
            stopRollerCommand()
        });
    }

    public static Command floorIntake (CommandXboxController driverController) {
        return new SequentialCommandGroup(new Command[]{
            elevatorToGoal(RobotContainer.ElevatorGoal.kIntake),
            runPivotAndHoldCommand(RobotContainer.IntakePivotGoal.kFloorPickup),
            runRollerCommand(),
            Commands.waitUntil(driverController.a() /*TODO: CHANGE TO FOLLOWING: kIntake.hasGamePiece()*/),
            stopRollerCommand(),
            runPivotAndHoldCommand(RobotContainer.IntakePivotGoal.kBase)
        });
    }

    /*
    return new FunctionalCommand(
        () -> {},
        () -> {},
        (interrupted) -> {},
        () -> false,
        elevator);
     */
}
