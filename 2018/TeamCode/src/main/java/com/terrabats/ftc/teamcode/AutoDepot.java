package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.vuforia.CameraDevice;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

@Autonomous(name = "TerraAuto: Depot")
public class AutoDepot extends LinearOpMode {
    TerraRunner bot = new TerraRunner();
    ElapsedTime timer = new ElapsedTime();
    private String goldPos = "c";
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";
    private static final String VUFORIA_KEY = "AWmKObX/////AAABmYb6oMBM9kDCg3sYoFPoVDBmzHX6yr4hMZ49TFMft1lZNH" +
            "brBnNlk1LQt0TnLInh/68lkCxQD6EHxPTOovDxYhjo4CkanvnpIU0HGctTwj9v0S3CSZqTHI0cOlT2SuollZoQkTMrdUY4y3YjtLbiPs" +
            "mYprc9994qPWPR3iltRb+IkHceIuG2yHxNhiNwbIwfllFtfmhXtDWk4Zw1MSI65lLb1qkAHmUE8vatCiT5QY4y8Fnx+MF0GRLM0W8TXH182agrf" +
            "+jBbLx2by32HRfh8dmCST0/Lj+g7Qccpsfv2x8B2tg+i95zYmSg0Slmr4hkZ3f4tIKpFUnFgdBWe08BD1Lt/N+jlerdrsdYM8wHx2ra";
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;
    boolean gold = false;

    @Override
    public void runOpMode() {
        bot.init(hardwareMap);

        while (bot.gyro.isCalibrating() && opModeIsActive()) {
            telemetry.addData("Is calibrating", "");
            telemetry.update();
        }

        initVuforia();
        initTfod();
        telemetry.addData("Done Calibrating", "Ready to Start");
        telemetry.update();
        waitForStart();
        timer.reset();
        hanging();
        getMineralPosition();
        telemetry.addData("MineralPos", goldPos);
        telemetry.update();

        switch (goldPos) {
            case "r":
                bot.turnDeg(-32, 0.5, TerraRunner.TurnMode.GYRO, this);
                bot.moveDis(28, 0.5, this); //go straight
                sleep(1000); //normal
                bot.turnDeg(60, 0.5, TerraRunner.TurnMode.GYRO, this);
                pause(0.7);
                bot.moveDis(30, 0.5, this); //go forward
                DropTM();
                sleep(1000); //normal
                bot.turnDeg(22, .5, TerraRunner.TurnMode.GYRO, this);
                pause(0.5);
                bot.move(0, 0);
                break;
            case "c":
                bot.moveDis(52, 0.5, this);
                DropTM();
                sleep(1000); //normal
                bot.turnDeg(54, .5, TerraRunner.TurnMode.GYRO, this);
                break;
            case "l":
                bot.turnDeg(28, 0.5, TerraRunner.TurnMode.GYRO, this);
                bot.moveDis(28, 0.5, this); //go straight
                sleep(1000); //normal
                bot.turnDeg(-45, 0.5, TerraRunner.TurnMode.GYRO, this);
                sleep(1000); //normal
                bot.moveDis(32, 0.5, this); //go forward
                DropTM();
                sleep(1000); //normal
                bot.turnDeg(72, .5, TerraRunner.TurnMode.GYRO, this);
                pause(0.5);
                bot.move(0, 0);
                break;
        }

        bot.moveDis(70, -.5, this);
        timer.reset();

        while (opModeIsActive() && timer.seconds() < 1.7) {
            bot.hang.setPower(-0.5);
        }
        bot.hang.setPower(0);


    }

    private void getMineralPosition() {
        int goldX = -1;
        int silverX = -1;
        int i = 0;
        if (opModeIsActive()) {
            tfod.activate();
            timer.reset();
            while (opModeIsActive() && i != 2) {
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    telemetry.addData("# Object Detected", updatedRecognitions.size());
                    if (updatedRecognitions.size() == 2) {
                        for (Recognition recognition : updatedRecognitions) {
                            if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                goldX = (int) recognition.getLeft();
                                telemetry.update();
                                gold = true;
                                i += 1;
                            } else {
                                if (recognition.getLabel().equals(LABEL_SILVER_MINERAL)) {
                                    silverX = (int) recognition.getLeft();
                                    telemetry.addLine("Silver Found");
                                    telemetry.update();
                                    i += 1;
                                }

                            }

                        }
                    }
                    telemetry.update();
                }
            }
            tfod.shutdown();
        }
        CameraDevice.getInstance().setFlashTorchMode(false);
        if (goldX == -1) {
            goldPos = "r";
        } else if (goldX > silverX) {
            goldPos = "c";
        } else {
            goldPos = "l";
        }
    }

    private void initVuforia() {
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
        CameraDevice.getInstance().setFlashTorchMode(false);
    }

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minimumConfidence = 0.65;

        tfodParameters.useObjectTracker = true;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }

    public void DropTM(LinearOpMode o) {
        timer.reset();
        while (o.opModeIsActive() && timer.seconds() < .3) {
            bot.move(1, 0);
        }
        bot.move(0, 0);
        timer.reset();
        while (o.opModeIsActive() && timer.seconds() < .2) {
            bot.move(-1, 0);
        }
        bot.move(0, 0);
        timer.reset();
    }

    public void pause(double x) {
        while (opModeIsActive() && timer.seconds() < x) {
        }
    }

    public void hanging() {
        while (opModeIsActive() && timer.seconds() < 1.6) {
            bot.hang.setPower(0.5);
        }
        bot.hang.setPower(0);

        bot.moveDis(12, 0.8, this);
        bot.turnDeg(-20, 0.5, TerraRunner.TurnMode.GYRO, this);
        timer.reset();
        pause(0.7);
        bot.turnDeg(19, 0.5, TerraRunner.TurnMode.GYRO, this);

    }

    public void DropTM() {
        while (opModeIsActive() && bot.getArmAngle() < 135) {
            bot.arm.setPower(0.5);
        }
        while (opModeIsActive() && bot.getArmAngle() > 135) {
            bot.arm.setPower(-0.5);
        }
    }
}



