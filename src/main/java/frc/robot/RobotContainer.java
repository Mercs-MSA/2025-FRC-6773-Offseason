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


import java.util.ArrayList;
import java.util.HashMap;

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
     
        } 

        else {
            //driverController.y().onTrue(Commands.runOnce(() -> robotDrive.resetGyro()));

            
        }
    }

    public EventLoop getTeleopEventLoop() {
        return teleopLoop;
    }
}