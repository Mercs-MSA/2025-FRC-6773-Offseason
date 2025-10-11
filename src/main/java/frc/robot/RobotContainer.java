// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.event.EventLoop;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;


import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.utils.debugging.LoggedTunableNumber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class RobotContainer {
    // Define subsystems
    
    // Define other utility classes
    
    private LoggedDashboardChooser<Command> autoChooser;
    
    public final CommandXboxController driverController = new CommandXboxController(0);
    public final CommandXboxController operatorController = new CommandXboxController(1);

    /* TODO: Set to true before competition
     please */

    private final boolean useCompetitionBindings = true;

    // Anshul said to use this because he loves event loops
    private final EventLoop teleopLoop = new EventLoop();
    
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

    public TeleopState currTeleopState = TeleopState.BASE;

    public RobotContainer() {
 
        // If using AdvantageKit, perform mode-specific instantiation of subsystems.
        switch (Constants.kCurrentMode) {
            case REAL:

                break;
            case SIM:

                break;
            default:

                break;
        }

        // Instantiate subsystems that don't care about mode, or are non-AdvantageKit enabled.
        // ex: LEDs = new LEDSubsystem();



        // Pass subsystems to classes that need them for configuration



        // Create any Dashboard choosers (LoggedDashboardChooser, etc)

        // Configure controls (drivebase suppliers, DriverStation triggers, Button and other Controller bindings)
        configureStateTriggers();
        configureButtonBindings();
    }

    /* Commands to schedule on telop start-up */
    public Command getTeleopCommand() {
        return new SequentialCommandGroup(
        );
    }

    public Command getAutonomousCommand() {

        return autoChooser.get();
    }

    public void getAutonomousExit() {
    }

 private void configureStateTriggers() {
        /* Due to roborio start up times sometimes modules aren't reset properly, this accounts for that */
        // new Trigger(DriverStation::isEnabled)
        //     .onTrue(
        //         /* Do not require robot drive or it will deschedule auto */
        //         );
    }

    private Command rumbleCommandOperator() {
        return Commands.startEnd(
            () -> operatorController.getHID().setRumble(RumbleType.kBothRumble, 1.0), 
            () -> operatorController.getHID().setRumble(RumbleType.kBothRumble, 0.0));

        
    }



    private Command rumbleCommandDriver() {
        return Commands.startEnd(
            () -> driverController.getHID().setRumble(RumbleType.kBothRumble, 1.0), 
            () -> driverController.getHID().setRumble(RumbleType.kBothRumble, 0.0));
    }

    private void configureButtonBindings() {
        ArrayList<Trigger> positionButtons = new ArrayList<Trigger>();
        positionButtons.add(operatorController.y());
        positionButtons.add(operatorController.b());
        positionButtons.add(operatorController.a());
        positionButtons.add(operatorController.x());



        //controls
        if (useCompetitionBindings) {
            driverController.rightTrigger().and(() -> currTeleopState == TeleopState.BASE).whileTrue(
                TeleopCommands.elevatorToGoal(ElevatorGoal.kIntake)
                .alongWith(TeleopCommands.runRollerCommandWhile()
                .onlyWhile(driverController.a() /*TODO: REPLACE THIS WITH kIntake.hasCoral() or whatever */)
                    .andThen(() -> {currTeleopState = TeleopState.FLOOR_TRANSFER; driverController.setRumble(RumbleType.kBothRumble, 0.5);})
                ).alongWith(TeleopCommands.runPivotAndHoldCommand(IntakePivotGoal.kFloorPickup))
            ).onFalse(
                TeleopCommands.stopElevator()
                .andThen(() -> {driverController.setRumble(RumbleType.kBothRumble, 0);})
                .andThen(TeleopCommands.runPivotAndHoldCommand(IntakePivotGoal.kBase))
                .andThen(
                    Commands.either(
                        TeleopCommands.runRollerCommandWhile().onlyWhile(driverController.a().negate() /*WOULD ACTUALLY BE BEAMBREAK.NEGATE() */)
                            .andThen(() -> {currTeleopState = TeleopState.ELEVATOR_READY;}), 
                        TeleopCommands.stopElevator()
                            .alongWith(TeleopCommands.stopRollerCommand()), 
                        () -> currTeleopState == TeleopState.FLOOR_TRANSFER
                    )
            ));
            
            operatorController.a().and(() -> currTeleopState == TeleopState.ELEVATOR_READY).whileTrue(
                TeleopCommands.elevatorToGoal(ElevatorGoal.kL2Coral)
                .andThen(() -> currTeleopState = TeleopState.SCORE_READY)
            ).onFalse(
                TeleopCommands.stopElevator()
            );

            operatorController.b().and(() -> currTeleopState == TeleopState.ELEVATOR_READY).whileTrue(
                TeleopCommands.elevatorToGoal(ElevatorGoal.kL3Coral)
                .andThen(() -> currTeleopState = TeleopState.SCORE_READY)
            )
            .onFalse(
                TeleopCommands.stopElevator()
            );

            operatorController.y().and(() -> currTeleopState == TeleopState.ELEVATOR_READY).whileTrue(
                TeleopCommands.elevatorToGoal(ElevatorGoal.kL4Coral)
                .andThen(() -> currTeleopState = TeleopState.SCORE_READY)
            )
            .onFalse(
                TeleopCommands.stopElevator()
            );

            operatorController.x().and(() -> currTeleopState == TeleopState.ELEVATOR_READY).whileTrue(
                TeleopCommands.elevatorToGoal(ElevatorGoal.kL1Coral)
                .andThen(() -> currTeleopState = TeleopState.SCORE_READY)
            )
            .onFalse(
                TeleopCommands.stopElevator()
            );

            operatorController.rightBumper().and(() -> currTeleopState == TeleopState.SCORE_READY).whileTrue(
                TeleopCommands.runRollerCommandWhile().onlyWhile(driverController.a()/*TODO: CHANGE TO BEAM BREAK ON MANIPULATOR */) /*TODO: CHANGE TO MANIPULATOR.RUNROLLER COMMAND*/
                .andThen(TeleopCommands.elevatorToGoal(ElevatorGoal.kStow))
                .andThen(() -> {currTeleopState = TeleopState.SCORE;})
            ).onFalse(
                TeleopCommands.stopRollerCommand()
                .alongWith(TeleopCommands.stopElevator())
            );

        } 
        else {
            //driverController.y().onTrue(Commands.runOnce(() -> robotDrive.resetGyro()));

            
        }
    }

    public EventLoop getTeleopEventLoop() {
        return teleopLoop;
    }
}