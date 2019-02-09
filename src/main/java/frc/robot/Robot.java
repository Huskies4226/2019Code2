/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.InterruptableSensorBase.WaitResult;
import edu.wpi.first.wpilibj.command.WaitUntilCommand;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends IterativeRobot {
  //private DifferentialDrive m_myRobot;
  RobotDrive drivetrain;
  Joystick leftStick;
  Joystick rightStick;
  JoystickButton button1;
  Spark motor1;
  Spark motor2;
  Encoder Encoder1;
  Integer height1;
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    //m_myRobot = new DifferentialDrive(new Victor(0), new Victor(1));
    motor1=new Spark(4);
    motor2=new Spark(5);
    drivetrain=new RobotDrive(0,1,2,3);
    leftStick=new Joystick(0);
    rightStick=new Joystick(1);
    button1= new JoystickButton(leftStick, 2);
    Encoder1=new Encoder(0,1,false);
    CameraServer.getInstance().startAutomaticCapture();
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    //basic auto
    drivetrain.tankDrive(-0.5,-0.5);
    timer(1);
    drivetrain.tankDrive(0,0);

    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    while(isOperatorControl()&&isEnabled()){
      
      NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
      NetworkTableEntry tx = table.getEntry("tx");
      NetworkTableEntry ty = table.getEntry("ty");
      NetworkTableEntry ta = table.getEntry("ta");
      double x = tx.getDouble(0.0);
      double y = ty.getDouble(0.0);
      double area = ta.getDouble(0.0);
      SmartDashboard.putNumber("LimelightX", x);
      SmartDashboard.putNumber("LimelightY", y);
      SmartDashboard.putNumber("LimelightArea", area);

          //create drive train
      double leftValue=leftStick.getRawAxis(1)*-1;
      double rightValue=leftStick.getRawAxis(5)*-1;
      //double height1=Encoder1.getDistance();
      //run drive train
      drivetrain.tankDrive(leftValue, rightValue, true);
      //create intake
      double intake=rightStick.getRawAxis(1);

      //run intake
      motor1.set(intake);

      double lift = rightStick.getRawAxis(5);
      motor2.set(lift*-1);


      //Limelight Autolineup
      while(leftStick.getRawButtonPressed(2)==1 && x<-0.5);
      {
          drivetrain.tankDrive(0,0.5);
      }
      while(leftStick.getRawButtonPressed(2)==1 && x>0.5);
        {
          drivetrain.tankDrive(0.5,0);
        }
      while(leftStick.getRawButtonPressed(2)==1 && x>-0.5 && x<0.5 && area<13);
      {
        drivetrain.tankDrive(0.5,0.5);
      }
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
    //Makes timer more accurate
    public void timer(int time){
      int m_time;
      m_time=time*1000;
      try {
        Thread.sleep(m_time);
      }
      catch(InterruptedException e1){
        e1.printStackTrace();
        DriverStation.reportWarning("failed to execute timer", true);
      }
}
  public void manualLift(){
    /*
    //running lift to certain heights
    if(rightStick.getRawButton(1)){
      motor2.set(.75);
      if(height1>100){
      motor2.set(0);}
    }
    //jog motor up for 2 seconds
    else if(rightStick.getRawButton(2)){
      //motor2.set(.75);
      //timer(2);
      //motor2.set(0);
      motor2.set(0.5);
    }
    //jog motor up for 3 seconds
    else if(rightStick.getRawButton(3)){
      //motor2.set(.75);
      //timer(3);
      //motor2.set(0);
      motor2.set(-0.5);
    }
    //jog motor up for 4 seconds
    else if(rightStick.getRawButton(4)){
      //motor2.set(.75);
      //timer(4);
      //motor2.set(0);
    }
    //stop motor upon release of button
    else{
      motor2.set(0);
    }
    */
  }
}