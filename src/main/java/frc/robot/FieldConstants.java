package frc.robot;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
//brady
public class FieldConstants {
    public static final Pose2d kReefCenter = new Pose2d(4.48249, Constants.kFieldWidthMeters / 2.0, Rotation2d.fromDegrees(0.0));
    public static final double kXNetLineMeters = 7.15;
    
    public static final Pose2d AL = new Pose2d(3.162, 4.222, Rotation2d.fromDegrees(0.0));
    public static final Pose2d AR = new Pose2d(3.162, 3.872, Rotation2d.fromDegrees(0.0));
    public static final Pose2d AM = new Pose2d(average(AL.getX(), AR.getX()), average(AL.getY(), AR.getY()), Rotation2d.fromDegrees(0.0));
    
    public static final Pose2d BL = new Pose2d(3.501, 2.998, Rotation2d.fromDegrees(60.0));
    public static final Pose2d BR = new Pose2d(3.906, 2.831, Rotation2d.fromDegrees(60.0));
    public static final Pose2d BM = new Pose2d(average(BL.getX(), BR.getX()), average(BL.getY(), BR.getY()), Rotation2d.fromDegrees(60.0));
    
    public static final Pose2d CL = new Pose2d(4.774, 2.693, Rotation2d.fromDegrees(120.0));
    public static final Pose2d CR = new Pose2d(5.097, 2.919, Rotation2d.fromDegrees(120.0));
    public static final Pose2d CM = new Pose2d(average(CL.getX(), CR.getX()), average(CL.getY(), CR.getY()), Rotation2d.fromDegrees(120.0));
    
    public static final Pose2d DL = new Pose2d(5.801, 3.822, Rotation2d.fromDegrees(180.3));
    public static final Pose2d DR = new Pose2d(5.801, 4.172, Rotation2d.fromDegrees(180.3));
    public static final Pose2d DM = new Pose2d(average(DL.getX(), DR.getX()), average(DL.getY(), DR.getY()), Rotation2d.fromDegrees(180.3));
    
    public static final Pose2d EL = new Pose2d(5.27, 5.11, Rotation2d.fromDegrees(-120.0));
    public static final Pose2d ER = new Pose2d(5.061, 5.208, Rotation2d.fromDegrees(-120.0));
    public static final Pose2d EM = new Pose2d(average(EL.getX(), ER.getX()), average(EL.getY(), ER.getY()), Rotation2d.fromDegrees(-120.0));
    
    public static final Pose2d FL = new Pose2d(4.012, 5.250, Rotation2d.fromDegrees(-60.0)); //y
    public static final Pose2d FR = new Pose2d(3.733, 5.112, Rotation2d.fromDegrees(-60.0)); //y
    public static final Pose2d FM = new Pose2d(average(FL.getX(), FR.getX()), average(FL.getY(), FR.getY()), Rotation2d.fromDegrees(-60.0));

    public static final Pose2d AD = new Pose2d(3.12, 4.386, Rotation2d.fromDegrees(0.0));          // reefABDescore
    public static final Pose2d BD = new Pose2d(3.487, 2.988, Rotation2d.fromDegrees(60.0));         // reefCDDescore
    public static final Pose2d CD = new Pose2d(4.867, 2.678, Rotation2d.fromDegrees(120.0));        // reefEFDescore
    public static final Pose2d DD = new Pose2d(5.859, 3.686, Rotation2d.fromDegrees(180.3));        // reefGHDescore
    public static final Pose2d ED = new Pose2d(5.456, 5.034, Rotation2d.fromDegrees(-120.0));       // reefIJDescore
    public static final Pose2d FD = new Pose2d(4.107, 5.391, Rotation2d.fromDegrees(-60.0));        // reefKLDescore

    public static final Pose2d B_IL = new Pose2d(1.103, 7.100, Rotation2d.fromDegrees(-55.0)); // SourceLeft
    public static final Pose2d B_IR = new Pose2d(1.0103, 0.926, Rotation2d.fromDegrees(55.0));

    public static final Pose2d R_IL = new Pose2d(1.103, 0.926, Rotation2d.fromDegrees(-55.0)); // SourceLeft
    public static final Pose2d R_IR = new Pose2d(1.0103, 7.1, Rotation2d.fromDegrees(55.0));




    

    
    public static double average(double a, double b) {
        return (a + b) / 2.0;
    }
    

}