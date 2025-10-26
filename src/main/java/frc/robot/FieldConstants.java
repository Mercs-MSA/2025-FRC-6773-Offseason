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
    
    public static final Pose2d BL = new Pose2d(3.67, 2.976, Rotation2d.fromDegrees(60.0));
    public static final Pose2d BR = new Pose2d(3.9617, 2.805, Rotation2d.fromDegrees(60.0));
    public static final Pose2d BM = new Pose2d(average(BL.getX(), BR.getX()), average(BL.getY(), BR.getY()), Rotation2d.fromDegrees(60.0));
    
    public static final Pose2d CL = new Pose2d(4.996, 2.790, Rotation2d.fromDegrees(120.0));
    public static final Pose2d CR = new Pose2d(5.28, 2.963, Rotation2d.fromDegrees(120.0));
    public static final Pose2d CM = new Pose2d(average(CL.getX(), CR.getX()), average(CL.getY(), CR.getY()), Rotation2d.fromDegrees(120.0));
    
    public static final Pose2d DL = new Pose2d(5.801, 3.822, Rotation2d.fromDegrees(180.3));
    public static final Pose2d DR = new Pose2d(5.801, 4.222, Rotation2d.fromDegrees(180.3));
    public static final Pose2d DM = new Pose2d(average(DL.getX(), DR.getX()), average(DL.getY(), DR.getY()), Rotation2d.fromDegrees(180.3));
    
    public static final Pose2d EL = new Pose2d(5.309, 5.058, Rotation2d.fromDegrees(-120.0));
    public static final Pose2d ER = new Pose2d(5.019, 5.240, Rotation2d.fromDegrees(-120.0));
    public static final Pose2d EM = new Pose2d(average(EL.getX(), ER.getX()), average(EL.getY(), ER.getY()), Rotation2d.fromDegrees(-120.0));
    
    public static final Pose2d FL = new Pose2d(3.985, 5.251, Rotation2d.fromDegrees(-60.0));
    public static final Pose2d FR = new Pose2d(3.696, 5.080, Rotation2d.fromDegrees(-60.0));
    public static final Pose2d FM = new Pose2d(average(FL.getX(), FR.getX()), average(FL.getY(), FR.getY()), Rotation2d.fromDegrees(-60.0));

    public static final Pose2d AD = new Pose2d(3.12, 4.386, Rotation2d.fromDegrees(0.0));          // reefABDescore
    public static final Pose2d BD = new Pose2d(3.487, 2.988, Rotation2d.fromDegrees(60.0));         // reefCDDescore
    public static final Pose2d CD = new Pose2d(4.867, 2.678, Rotation2d.fromDegrees(120.0));        // reefEFDescore
    public static final Pose2d DD = new Pose2d(5.859, 3.686, Rotation2d.fromDegrees(180.3));        // reefGHDescore
    public static final Pose2d ED = new Pose2d(5.456, 5.034, Rotation2d.fromDegrees(-120.0));       // reefIJDescore
    public static final Pose2d FD = new Pose2d(4.107, 5.391, Rotation2d.fromDegrees(-60.0));        // reefKLDescore

    public static final Pose2d B_IL = new Pose2d(0.84, 6.74, Rotation2d.fromDegrees(-55.0)); // SourceLeft
    public static final Pose2d B_IR = new Pose2d(1.49, 0.6, Rotation2d.fromDegrees(55.0));
    public static final Pose2d R_IL = new Pose2d(16.7, 1.25, Rotation2d.fromDegrees(127.18)); // SourceLeft
    public static final Pose2d R_IR = new Pose2d(16.16, 7.33, Rotation2d.fromDegrees(55.0+180));



    /*
    R_IL: 16.7, 1.25, 127.18
    R_IR: 16.16 7.33 55+180

    
    */
    

    
    public static double average(double a, double b) {
        return (a + b) / 2.0;
    }
    

}