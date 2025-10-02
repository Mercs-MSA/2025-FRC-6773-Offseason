// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package frc.robot;

import frc.robot.subsystems.drive.controllers.*;

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
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOPigeon2;
import frc.robot.subsystems.drive.ModuleIOKraken;
import frc.robot.subsystems.drive.Module;
import frc.robot.subsystems.drive.ModuleIO;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.Drive.DriveState;
import frc.robot.subsystems.drive.controllers.GoalPoseChooser;
import frc.robot.subsystems.drive.controllers.GoalPoseChooser.SIDE;

import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionConstants;
import frc.robot.subsystems.vision.VisionIOLimelight;
import frc.robot.subsystems.vision.CameraIO;

import static frc.robot.subsystems.drive.DriveConstants.*;
import static frc.robot.subsystems.vision.VisionConstants.camera0Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class RobotContainer {
    // Define subsystems
    private final Drive robotDrive;
    private final Vision vision;
    
    // Define other utility classes
    
    private LoggedDashboardChooser<Command> autoChooser;
    
    private final CommandXboxController driverController = new CommandXboxController(0);
    private final CommandXboxController operatorController = new CommandXboxController(1);

    /* TODO: Set to true before competition
     please */

    private final boolean useCompetitionBindings = true;

    // Anshul said to use this because he loves event loops
    private final EventLoop teleopLoop = new EventLoop();

    public RobotContainer() {


        // If using AdvantageKit, perform mode-specific instantiation of subsystems.
        switch (Constants.kCurrentMode) {
            case REAL:
            robotDrive = new Drive(
                new Module[] {
                    new Module("FL", new ModuleIOKraken(kFrontLeftHardware)),
                    new Module("FR", new ModuleIOKraken(kFrontRightHardware)),
                    new Module("BL", new ModuleIOKraken(kBackLeftHardware)),
                    new Module("BR", new ModuleIOKraken(kBackRightHardware))
                },
                new GyroIOPigeon2(),
                null
            );

            vision = new Vision(new CameraIO[] {
                new VisionIOLimelight(camera0Name, () -> robotDrive.getRobotRotation()),
            });

            robotDrive.setVision(vision);
            break;


            case SIM:

               robotDrive = new Drive( new Module[] {
                    new Module("FL", new ModuleIOSim()),
                    new Module("FR", new ModuleIOSim()),
                    new Module("BL", new ModuleIOSim()),
                    new Module("BR", new ModuleIOSim())
                }, new GyroIO() {}, null);

                vision = new Vision(new CameraIO[] {
                    new VisionIOLimelight(camera0Name, () -> robotDrive.getRobotRotation()),
                });
    
                robotDrive.setVision(vision);



                break;
            default:
                
               robotDrive = new Drive( new Module[] {
                    new Module("FL", new ModuleIO() {}),
                    new Module("FR", new ModuleIO() {}),
                    new Module("BL", new ModuleIO() {}),
                    new Module("BR", new ModuleIO() {})
                }, new GyroIO() {}, null);

                vision = new Vision(new CameraIO[] {
                    new VisionIOLimelight(camera0Name, () -> robotDrive.getRobotRotation()),
                });
    
                robotDrive.setVision(vision);


                break;
        }

        // Instantiate subsystems that don't care about mode, or are non-AdvantageKit enabled.
        // ex: LEDs = new LEDSubsystem();


        robotDrive.setDefaultCommand(Commands.run(() -> robotDrive.setDriveState(DriveState.TELEOP), robotDrive));

        // Pass subsystems to classes that need them for configuration
        robotDrive.acceptJoystickInputs(
            () -> - driverController.getLeftY(),
            () -> - driverController.getLeftX(),
            () -> - driverController.getRightX(),
            () -> driverController.getHID().getPOV());


        // Create any Dashboard choosers (LoggedDashboardChooser, etc)

        // Configure controls (drivebase suppliers, DriverStation triggers, Button and other Controller bindings)
        configureStateTriggers();
        configureButtonBindings();
    }

    /* Commands to schedule on telop start-up */
    public Command getTeleopCommand() {
        return new SequentialCommandGroup(
            robotDrive.setDriveStateCommand(DriveState.TELEOP)
        );
    }

    public Command getAutonomousCommand() {
        Commands.runOnce(() -> robotDrive.setDriveState(DriveState.AUTON), robotDrive).schedule();

        return autoChooser.get();
    }

    public void getAutonomousExit() {
        robotDrive.setDriveState(DriveState.STOP);
    }

 private void configureStateTriggers() {
        /* Due to roborio start up times sometimes modules aren't reset properly, this accounts for that */
        new Trigger(DriverStation::isEnabled)
            .onTrue(
                /* Do not require robot drive or it will deschedule auto */
                Commands.runOnce(() -> robotDrive.resetModulesEncoders()));
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
            driverController.y().onTrue(Commands.runOnce(() -> robotDrive.resetGyro()));


            driverController.x()
                 .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_INTAKE))
                 .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            driverController.leftBumper()
                .onTrue(GoalPoseChooser.setSideCommand(SIDE.LEFT))
                .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_CORAL))
                .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            driverController.rightBumper()
                .onTrue(GoalPoseChooser.setSideCommand(SIDE.LEFT))
                .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.DRIVE_TO_CORAL))
                .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

    

            
        } 

        else {
            driverController.y().onTrue(Commands.runOnce(() -> robotDrive.resetGyro()));

            driverController.x()
                .onTrue(robotDrive.setDriveStateCommand(DriveState.SYSID_CHARACTERIZATION).andThen(Commands.run(() -> 
                    robotDrive.runMOICharacterization(20), robotDrive)))
                .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // driverController.x()
            //     .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.TELEOP_SNIPER))
            //     .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            // getPOV == -1 if nothing is pressed, so if it doesn't return that
            // then pov control is being used as its being pressed
            new Trigger(()-> driverController.getHID().getPOV() != -1)
                .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.POV_SNIPER))
                .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

            driverController.b()
                .onTrue(robotDrive.setDriveStateCommandContinued(DriveState.LINEAR_TEST))
                .onFalse(robotDrive.setDriveStateCommand(DriveState.TELEOP));

      
        }
    }

    public EventLoop getTeleopEventLoop() {
        return teleopLoop;
    }
}