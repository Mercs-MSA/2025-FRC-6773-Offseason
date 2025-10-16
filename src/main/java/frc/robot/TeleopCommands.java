package frc.robot;

import frc.robot.subsystems.elevator.Elevator;
// import frc.robot.subsystems.elevator.Elevator;
// import frc.robot.subsystems.elevator.Elevator.ElevatorGoal;
import frc.robot.subsystems.intake.Intake;
// import frc.robot.subsystems.intake.Intake.Gamepiece;
import frc.robot.subsystems.intake.Intake.IntakePivotGoal;
import frc.robot.subsystems.manipulator.Manipulator;

import java.lang.annotation.ElementType;

// import frc.robot.subsystems.intake.Intake.RollerGoal;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
// import frc.robot.subsystems.climb.Climb;
// import frc.robot.subsystems.climb.Climb.ClimbVoltageGoal;
import frc.robot.utils.debugging.LoggedTunableNumber;

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
    private final Intake kIntake;
    private final Manipulator kManipulator;
    private final Elevator kElevator;
    // private final Climb kClimb;

    private CommandXboxController kController;

    /** 
     * Internal state to decide whether or not to stop the rollers when the intake's 
     * stop method is invoked. When creating a command that requires the rollers, this
     * variable should be set to true when the rollers are running then set to false
     * when they should no longer run
     */
    private boolean stopRollers = false;
    /** 
     * Internal state to decide whether or not to stop the algae picker when the intake's 
     * stop method is invoked. When creating a command that requires the algae picker, this
     * variable should be set to true when the algae picker is running then set to false
     * when it should no longer run
     */
    private boolean stopPivot = false;

    /**
     * Creates a new TeleopCommands factory
     * 
     * @param elevator The elevator subsystem instance
     * @param intake The intake subsystem intance
     * @param climb The climb subsystem instance
     */
    public TeleopCommands(Intake intake, Elevator elevator, Manipulator manipulator, CommandXboxController controller) {
        // kElevator = elevator;
        kIntake = intake;
        kElevator = elevator;
        kManipulator = manipulator;
        kController = controller;
        // kClimb = climb;
    }

    /**
     * Runs the algae picker pivot and then stops it as well as the rollers, this command should 
     * be decorated with an end condition specified by the caller
     * 
     * @param pivotGoal The algae picker pivot goal
     * @return The command to start the algae picker pivot and stop the entire intake
     */
    public Command runPivotAndStopIntakeCommand(IntakePivotGoal pivotGoal) {
        // Trigger coralTrigger = new Trigger(() -> kManipulator.getCoralDetected());

        return Commands.startEnd(
            ()-> {
                stopPivot = false;
                stopRollers = false;
                kIntake.setPivotGoal(pivotGoal);
            }, 
            () -> {
                stopPivot = true;
                stopRollers = true;
                kIntake.stop(stopRollers, stopPivot);
            }, 
            kIntake);
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

    public Command runPivotSubstationAndHoldCommand() {
        return Commands.startEnd(
            () -> {
                stopPivot = false;
                kIntake.substationIntakeCommand(kController);
            }, 
            () -> {
                stopPivot = false;
                kIntake.setPivotPosition(kIntake.getPivotPosition());
            }, 
            kIntake);
    }

    public Command floorIntakeCommand() {
        return Commands.parallel(
            runPivotAndHoldCommand(IntakePivotGoal.kFloorPickup),
            runRollerCommand(),
            runManipulatorRollersCommand()
        );
    }

    public Command substationIntakeCommand() {
        return Commands.parallel(
            runPivotSubstationAndHoldCommand(),
            runRollerCommand(),
            runManipulatorRollersCommand()
        );
    }

    public Command stowCommand() {
        // return runPivotAndHoldCommand(kManipulator.getCoralDetected() ? IntakePivotGoal.kStow : IntakePivotGoal.kTransfer);
        return Commands.parallel(runPivotAndHoldCommand(IntakePivotGoal.kStow), runRollerCommand());
            
            
        
    }

    // public Command checkManipulatorCommand() {
    //     return Commands.waitUntil(() -> kManipulator.getCoralDetected())
    //         .andThen(Commands.runOnce(() -> {
    //             stopRollerCommand();
    //         }));
    // }

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

    public Command stowRollerCommand() {
        return Commands.runOnce(() -> {
            kIntake.stow();
        });
    }

    public Command runRollerCommand() {
        return Commands.run(() -> {
            stopRollers = false;
            if (!kManipulator.getCoralDetected()) { kIntake.runRollers(); } else { kIntake.stopRollers(); };
        });
    }

    public Command runSubstationPickupCommand() {
        return Commands.run(() -> {
            //stopRollers = false;
            if (!kIntake.getCoralDetected()) { kIntake.setPivotGoal(IntakePivotGoal.kSubstationPickup); } else if (kIntake.getCoralDetected() || kIntake.ifStowed()) {  kIntake.setPivotGoal(IntakePivotGoal.kStow); };
        });
    }


    public Command toggleRollerCommand() {
        return Commands.runOnce(() -> {
            stopRollers = !stopRollers;
            if (stopRollers) {
                kIntake.stop(true, false);
            } else {
                kIntake.runRollers();
            }
        });
    }


    /**
     * Stops the intake rollers. This will also stop the pivot if the
     * stopPivot variable is set to true
     * 
     * @return The command to stop the rollers that runs once
     */
    // public Command stopRollersCommand() {
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
    public Command stopPivotCommand() {
        // Note that the state must be set via command and not in method since the method
        // only returns an instance of the command and does not run its internal logic
        return setStopPivotStateCommand(true)
            .andThen(
                Commands.runOnce(() -> kIntake.stop(stopRollers, stopPivot), kIntake));
    }

    public Command stopRollerCommand() {
        return setStopRollerStateCommand(true).andThen(Commands.runOnce(() -> kIntake.stop(true, false)));
    }

    // public Command stopRollersAndPivotCommand() {
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
    private Command setStopPivotStateCommand(boolean stopPivotState) {
        return Commands.runOnce(() -> stopPivot = stopPivotState);
    }

    private Command setStopRollerStateCommand(boolean stopRollerState) {
        return Commands.runOnce(() -> stopRollers = stopRollerState);
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