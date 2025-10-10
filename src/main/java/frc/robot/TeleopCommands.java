
package frc.robot;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

// import frc.robot.subsystems.elevator.Elevator;
// import frc.robot.subsystems.elevator.Elevator.ElevatorGoal;
// import frc.robot.subsystems.intake.Intake;
// import frc.robot.subsystems.intake.Intake.Gamepiece;
// import frc.robot.subsystems.intake.Intake.IntakePivotGoal;
// import frc.robot.subsystems.intake.Intake.RollerGoal;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;
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


    public enum ElevatorGoal {
        kL4Coral(() -> Units.inchesToMeters(55.0)),
        kL3Coral(() -> 0.84),
        kL2Coral(() -> Units.inchesToMeters(20)),
        kL1Coral(() -> Units.inchesToMeters(5.0)),
        kL205Coral(() -> 0.7),

        kBarge(() -> Units.inchesToMeters(60.0)),
        kL3Algae(() -> 1.02 + Units.inchesToMeters(0.5)), //kL3Algae(() -> 0.7 - 0.1),
        kL2Algae(() -> 0.65),// kL2Algae(() -> 0.3 - 0.1),
        kProcessor(() -> 0.3 - 0.15),
        kGroundAlgae(() -> Units.inchesToMeters(8.0)),
        /** Stow the elevator during transit */
        kStow(() -> Units.inchesToMeters(0)),
        /** Position for intaking from the coral station */
        kIntake(() -> Units.inchesToMeters(0.0)),
        /** Custom setpoint that can be modified over network tables; Usefu for debugging */
        custom(new LoggedTunableNumber("Elevator/Custom", Units.inchesToMeters(8.0)));
        
        private DoubleSupplier goalMeters;
        private 

        ElevatorGoal(DoubleSupplier goalMeters) {
            this.goalMeters = goalMeters;
        }

        public double getGoalMeters() {
            return this.goalMeters.getAsDouble();
        }
    } 
    public enum IntakePivotGoal {
        kFloorPickup(-70.0),
        kStationPickup(-10.0),
        kBase(0.0),
        temp(-30.0); // temporary value until we get the real one
        public final double angle;

        IntakePivotGoal(double angle) {
            this.angle = angle;
        }
    }

    public enum TeleopState {
        BASE(IntakePivotGoal.temp),
        FLOOR_INTAKE(IntakePivotGoal.kFloorPickup),
        STATION_INTAKE(IntakePivotGoal.kStationPickup),
        FLOOR_TRANSFER(IntakePivotGoal.temp),
        PIVOT_WITH_CORAL(IntakePivotGoal.temp),
        ELEVATOR_READY(IntakePivotGoal.temp),
        SCORE_READY(IntakePivotGoal.temp),
        SCORE(IntakePivotGoal.temp),
        RETURNING_TO_BASE(IntakePivotGoal.temp);

        public final IntakePivotGoal pivotGoal;

        TeleopState(IntakePivotGoal pivotGoal) {
            this.pivotGoal = pivotGoal;
        }
    }

    public enum TeleopChain {
        BASE(new TeleopState[] {TeleopState.BASE}),
        FLOOR_CYCLE(new TeleopState[] {TeleopState.FLOOR_INTAKE, TeleopState.FLOOR_TRANSFER, TeleopState.PIVOT_WITH_CORAL}),
        STATION_CYCLE(new TeleopState[] {TeleopState.STATION_INTAKE, TeleopState.PIVOT_WITH_CORAL}),
        SCORE(new TeleopState[] {TeleopState.ELEVATOR_READY, TeleopState.SCORE_READY, TeleopState.SCORE, TeleopState.RETURNING_TO_BASE});

        public final TeleopState[] initialState;
        TeleopChain(TeleopState[] initialState) {
            this.initialState = initialState;
        }
    }

    // private final Elevator kElevator;
    // private final Intake kIntake;
    // private final Climb kClimb;

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

    public TeleopState currentState = TeleopState.BASE;


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
    public Command runPivotAndStopIntakeCommand(IntakePivotGoal pivotGoal) {
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

    public Command runPivotAndHoldCommand(IntakePivotGoal pivotGoal) {
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

    public Command runRollerCommand() {
        return Commands.runOnce(() -> {
            stopRollers = false;
            // kIntake.runRollers();
        });
    }

    public Command toggleRollerCommand() {
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
                // Commands.runOnce(() -> kIntake.stop(stopRollers, stopPivot), kIntake)
                );
    }

    public Command stopRollerCommand() {
        return setStopRollerStateCommand(true).andThen(
            // Commands.runOnce(() -> kIntake.stop(true, false))
            );
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


    public Command elevatorToGoal(ElevatorGoal goal) {
        return Commands.runOnce(() -> System.out.println() /*kElevator.setGoal(goal)*/);
    }

    public SequentialCommandGroup toBase () {
        return new SequentialCommandGroup(new Command[]{
            runPivotAndHoldCommand(IntakePivotGoal.kBase),
            elevatorToGoal(ElevatorGoal.kIntake),
            stopRollerCommand()
        });
    }

    public SequentialCommandGroup floorIntake (RobotContainer cRobotContainer) {
        return new SequentialCommandGroup(new Command[]{
            elevatorToGoal(ElevatorGoal.kIntake),
            runPivotAndHoldCommand(IntakePivotGoal.kFloorPickup),
            runRollerCommand(),
            Commands.waitUntil(cRobotContainer.driverController.a() /*TODO: CHANGE TO FOLLOWING: k*/),
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
