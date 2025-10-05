// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Voltage;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.Elevator.Elevator;
import frc.robot.subsystems.Elevator.ElevatorConstants;
import frc.robot.subsystems.Elevator.ElevatorIO;
import frc.robot.subsystems.Elevator.ElevatorIOSim;
import frc.robot.Constants;
import frc.robot.subsystems.Elevator.ElevatorIOTalonFX;
import frc.robot.subsystems.Elevator.Elevator.ElevatorGoal;
import frc.robot.utils.debugging.SysIDCharacterization;
import pabeles.concurrency.ConcurrencyOps.NewInstance;

public class RobotContainer {
    // Define subsystems
    
    // Define other utility classes
    
    private LoggedDashboardChooser<Command> autoChooser;
    private final CommandXboxController driverController = new CommandXboxController(0);
    private final CommandXboxController operatorController = new CommandXboxController(1);
    private final Elevator m_Elevator;


    /* TODO: Set to true before competition
     please */

    private final boolean useCompetitionBindings = true;

    // Anshul said to use this because he loves event loops
    private final EventLoop teleopLoop = new EventLoop();

    public RobotContainer() {
        switch (Constants.kCurrentMode) {
            case REAL:
                m_Elevator = new Elevator(new ElevatorIOTalonFX(ElevatorConstants.kRoboElevatorHardware, ElevatorConstants.kMotorConfiguration, ElevatorConstants.kElevatorGains));   
                break;
            case SIM:
                m_Elevator = new Elevator(new ElevatorIOSim(ElevatorConstants.kRoboElevatorHardware, ElevatorConstants.kSimulationConfiguration, ElevatorConstants.kElevatorGains, 0.0, 10.0, 1.0));
                break;
            default:
                m_Elevator = new Elevator(new ElevatorIOSim(null, null, null, 0, 0, 0));
                break;
        }
        // Instantiate subsystems that don't care about mode, or are non-AdvantageKit enabled.
        // ex: LEDs = new LEDSubsystem();

        // Create any Dashboard choosers (LoggedDashboardChooser, etc)

        // Configure controls (drivebase suppliers, DriverStation triggers, Button and other Controller bindings)
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
        //         /* Do not require robot drive or it will deschedule auto */);
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



        if (useCompetitionBindings) {
            driverController.y().onTrue(new InstantCommand(() -> m_Elevator.setGoal(ElevatorGoal.kL2Coral)));
            driverController.a().onTrue(new InstantCommand(() -> m_Elevator.setGoal(ElevatorGoal.kStow)));
            driverController.b().onTrue(new InstantCommand(() -> m_Elevator.setGoal(ElevatorGoal.kL4Coral)));
            driverController.rightBumper().onTrue(new InstantCommand(() -> m_Elevator.setGoal(ElevatorGoal.custom)));
            // driverController.y().onTrue(Commands.runOnce(() -> robotDrive.resetGyro()));

            // getPOV == -1 if nothing is pressed, so if it doesn't return that
            // then pov control is being used as its being pressed
            // new Trigger(()-> driverController.getHID().getPOV() != -1)
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.POV_SNIPER))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.a()
            //     .onTrue(GoalPoseChooser.setSideCommand(SIDE.ALGAE)
            //     .andThen(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_ALGAE)))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));


            // driverController.b()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_BARGE))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.x()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_INTAKE))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.leftBumper()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.LEFT))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.rightBumper()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.RIGHT))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.leftTrigger()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.UP))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.rightTrigger()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DOWN))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // BOI ignore ts //
            // driverController.rightBumper()
            // .onTrue(Commands.runOnce(() -> intake.setPivotVoltage(-1)))
            // .onFalse(Commands.runOnce(() -> intake.setPivotVoltage(0)));

            // driverController.leftBumper()
            // .onTrue(Commands.runOnce(() -> intake.setPivotVoltage(1)))
            // .onFalse(Commands.runOnce(() -> intake.setPivotVoltage(0)));

            
        } 

        else {
            driverController.x().onTrue(SysIDCharacterization.runElevatorSysIDTests((voltage) -> m_Elevator.setVoltage(voltage), m_Elevator));
            // driverController.x()
            //     .onTrue(robotDrive.setDriveStateCommand(DriveState.SYSID_CHARACTERIZATION).andThen(Commands.run(() -> 
            //         robotDrive.runMOICharacterization(20), robotDrive)))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // // driverController.x()
            // //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.TELEOP_SNIPER))
            // //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // // getPOV == -1 if nothing is pressed, so if it doesn't return that
            // // then pov control is being used as its being pressed
            // new Trigger(()-> driverController.getHID().getPOV() != -1)
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.POV_SNIPER))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.b()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.LINEAR_TEST))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.a()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_BARGE))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));
        }
    }

    public EventLoop getTeleopEventLoop() {
        return teleopLoop;
    }
}