// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;
import edu.wpi.first.math.Pair;
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
// import frc.robot.subsystems.drive.Drive;
// import frc.robot.subsystems.drive.GyroIO;
// import frc.robot.subsystems.drive.GyroIOPigeon2;
// import frc.robot.subsystems.drive.ModuleIOKraken;
// import frc.robot.subsystems.drive.Module;
// import frc.robot.subsystems.drive.ModuleIO;
// import frc.robot.subsystems.drive.ModuleIOSim;
// import frc.robot.subsystems.drive.Drive.DriveState;
// import frc.robot.subsystems.drive.controllers.GoalPoseChooser;
// import frc.robot.subsystems.drive.controllers.GoalPoseChooser.SIDE;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.IntakeConstants;
import frc.robot.subsystems.intake.IntakePivotIO;
import frc.robot.subsystems.intake.IntakePivotIOSim;
import frc.robot.subsystems.intake.IntakePivotIOTalonFX;
import frc.robot.subsystems.intake.IntakeRollerIO;
import frc.robot.subsystems.intake.IntakeRollerIOSim;
import frc.robot.subsystems.intake.IntakeRollerIOTalonFX;
import frc.robot.subsystems.intake.Intake.IntakePivotGoal;
import frc.robot.TeleopCommands;

// import static frc.robot.subsystems.drive.DriveConstants.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
public class RobotContainer {
    // Define subsystems
    // private final Drive robotDrive;
     private final Intake intake;

    
    // Define other utility classes
    
    private LoggedDashboardChooser<Command> autoChooser;
    
    private final CommandXboxController driverController = new CommandXboxController(0);
    private final CommandXboxController operatorController = new CommandXboxController(1);

    /* TODO: Set to true before competition
     please */

    private final boolean useCompetitionBindings = true;

    // Anshul said to use this because he loves event loops
    private final EventLoop teleopLoop = new EventLoop();

    private final TeleopCommands teleopCommands;


    public RobotContainer() {


        // If using AdvantageKit, perform mode-specific instantiation of subsystems.
        switch (Constants.kCurrentMode) {
            case REAL:
            //    robotDrive = new Drive( new Module[] {
            //         new Module("FL", new ModuleIOKraken(kFrontLeftHardware )),
            //         new Module("FR", new ModuleIOKraken(kFrontRightHardware)),
            //         new Module("BL", new ModuleIOKraken(kBackLeftHardware  )),
            //         new Module("BR", new ModuleIOKraken(kBackRightHardware ))
            //     }, new GyroIOPigeon2());

                intake = new Intake(
                    new IntakePivotIOTalonFX(
                        IntakeConstants.kPivotMotorHardware,
                        IntakeConstants.kPivotMotorConfiguration,
                        IntakeConstants.kPivotGains,
                        IntakeConstants.kStatusSignalUpdateFrequencyHz),
                    new IntakeRollerIOTalonFX(
                        IntakeConstants.kRollerMotorHardware,
                        IntakeConstants.kRollerMotorConfiguration,
                        IntakeConstants.kStatusSignalUpdateFrequencyHz));
                break;
            case SIM:
            //    robotDrive = new Drive( new Module[] {
            //         new Module("FL", new ModuleIOSim()),
            //         new Module("FR", new ModuleIOSim()),
            //         new Module("BL", new ModuleIOSim()),
            //         new Module("BR", new ModuleIOSim())
            //     }, new GyroIO() {});

                intake = new Intake(
                    new IntakePivotIOSim(
                        0.02,
                        IntakeConstants.kPivotMotorHardware,
                        IntakeConstants.kPivotSimulationConfiguration,
                        IntakeConstants.kPivotGains),
                    new IntakeRollerIOSim(
                        0.02,
                        IntakeConstants.kRollerMotorHardware,
                        IntakeConstants.kIntakeRollerSimulationConfiguration));
                break;
            default:
            //    robotDrive = new Drive( new Module[] {
            //         new Module("FL", new ModuleIO() {}),
            //         new Module("FR", new ModuleIO() {}),
            //         new Module("BL", new ModuleIO() {}),
            //         new Module("BR", new ModuleIO() {})
            //     }, new GyroIO() {});

                intake = new Intake(new IntakePivotIO(){}, new IntakeRollerIO(){});

                break;
        }

        // Instantiate subsystems that don't care about mode, or are non-AdvantageKit enabled.
        // ex: LEDs = new LEDSubsystem();
        teleopCommands = new TeleopCommands(intake);



        // robotDrive.setDefaultCommand(Commands.run(() -> robotDrive.setDriveState(DriveState.TELEOP), robotDrive));

        // Pass subsystems to classes that need them for configuration
        // robotDrive.acceptJoystickInputs(
        //     () -> - driverController.getLeftY(),
        //     () -> - driverController.getLeftX(),
        //     () -> - driverController.getRightX(),
        //     () -> driverController.getHID().getPOV());


        // Create any Dashboard choosers (LoggedDashboardChooser, etc)

        // Configure controls (drivebase suppliers, DriverStation triggers, Button and other Controller bindings)
        configureButtonBindings();
    }

    /* Commands to schedule on telop start-up */


    public Command getAutonomousCommand() {

        return autoChooser.get();
    }

    public void getAutonomousExit() {
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
            // driverController.y().onTrue(Commands.runOnce(() -> robotDrive.resetGyro()));

            driverController.a()
                .whileTrue(teleopCommands.runPivotAndHoldCommand(IntakePivotGoal.kFloorPickup))
                .onFalse(teleopCommands.runPivotAndHoldCommand(IntakePivotGoal.kStow));

            driverController.b()
                .whileTrue(teleopCommands.runPivotAndHoldCommand(IntakePivotGoal.kStationPickup))
                .onFalse(teleopCommands.runPivotAndHoldCommand(IntakePivotGoal.kStow));

            driverController.rightBumper()
                .onTrue(teleopCommands.toggleRollerCommand());

            
        } 
    }

        


    public EventLoop getTeleopEventLoop() {
        return teleopLoop;
    }
}