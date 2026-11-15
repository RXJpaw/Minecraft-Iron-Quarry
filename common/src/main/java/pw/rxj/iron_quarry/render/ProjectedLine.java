package pw.rxj.iron_quarry.render;

import com.mojang.datafixers.util.Pair;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectedLine {
    private final SpriteVec2f line;
    private final Vector4f clipFrom;
    private final Vector4f clipTo;
    private final float aspectRatio;

    private ProjectedLine(SpriteVec2f line, Vector4f clipFrom, Vector4f clipTo, float aspectRatio) {
        this.line = line;
        this.clipFrom = clipFrom;
        this.clipTo = clipTo;
        this.aspectRatio = aspectRatio;
    }

    private static ProjectedLine from(SpriteVec2f line, Matrix4fc clipMatrix, float aspectRatio) {
        return new ProjectedLine(line, clipMatrix.transform(new Vector4f(line.from, 1.0F)), clipMatrix.transform(new Vector4f(line.to, 1.0F)), aspectRatio);
    }
    public static Optional<ProjectedLine> of(SpriteVec2f line, Matrix4fc clipMatrix, float aspectRatio, float minClipW, float screenMargin) {
        return from(line, clipMatrix, aspectRatio).cropToFront(clipMatrix, minClipW).flatMap(projectedLine -> projectedLine.cropToScreen(clipMatrix, screenMargin));
    }

    public List<SpriteVec2f> dash(Vector3f eye, float dashAngle, float gapAngle) {
        List<SpriteVec2f> dashes = new ArrayList<>();

        double footFraction = this.line.footFractionOf(eye);
        double eyeDistance = this.line.pointAt(footFraction).distance(eye) / this.line.innerDistance();
        double startAngle = Math.atan(-footFraction / eyeDistance);
        double endAngle = Math.atan((1.0 - footFraction) / eyeDistance);
        double period = dashAngle + gapAngle;
        if(period <= 0.0) return dashes;

        for (double angle = Math.floor(startAngle / period) * period; angle < endAngle; angle += period) {
            double start = Math.max(angle, startAngle);
            double end = Math.min(angle + dashAngle, endAngle);

            if(end > start) dashes.add(SpriteVec2f.from(this.atAngle(footFraction, eyeDistance, start), this.atAngle(footFraction, eyeDistance, end)));
        }

        return dashes;
    }

    private Optional<ProjectedLine> cropToFront(Matrix4fc clipMatrix, float minClipW) {
        boolean fromInFront = this.clipFrom.w >= minClipW;
        boolean toInFront = this.clipTo.w >= minClipW;
        if(fromInFront && toInFront) return Optional.of(this);
        if(!fromInFront && !toInFront) return Optional.empty();

        Vector3f crossing = this.line.pointAt((minClipW - this.clipFrom.w) / (this.clipTo.w - this.clipFrom.w));
        SpriteVec2f front = fromInFront ? SpriteVec2f.from(this.line.from, crossing) : SpriteVec2f.from(crossing, this.line.to);

        return Optional.of(from(front, clipMatrix, this.aspectRatio));
    }
    private Optional<ProjectedLine> cropToScreen(Matrix4fc clipMatrix, float screenMargin) {
        double fromX = this.getScreenX(this.clipFrom);
        double fromY = this.getScreenY(this.clipFrom);
        Pair<Double, Double> rangeX = getAxisRange(fromX, this.getScreenX(this.clipTo) - fromX, this.aspectRatio * screenMargin);
        Pair<Double, Double> rangeY = getAxisRange(fromY, this.getScreenY(this.clipTo) - fromY, screenMargin);

        double start = Math.max(0.0, Math.max(rangeX.getFirst(), rangeY.getFirst()));
        double end = Math.min(1.0, Math.min(rangeX.getSecond(), rangeY.getSecond()));
        if(start >= end) return Optional.empty();

        return Optional.of(from(SpriteVec2f.from(this.atScreenFraction(start), this.atScreenFraction(end)), clipMatrix, this.aspectRatio));
    }
    private static Pair<Double, Double> getAxisRange(double start, double delta, double limit) {
        if(delta == 0.0) {
            return Math.abs(start) <= limit ? Pair.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) : Pair.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        }

        double enter = (-limit - start) / delta;
        double exit = (limit - start) / delta;

        return Pair.of(Math.min(enter, exit), Math.max(enter, exit));
    }

    private double getScreenX(Vector4f clipPos) {
        return clipPos.x / clipPos.w * this.aspectRatio;
    }
    private double getScreenY(Vector4f clipPos) {
        return clipPos.y / clipPos.w;
    }
    private Vector3f atAngle(double footFraction, double eyeDistance, double angle) {
        return this.line.pointAt(footFraction + eyeDistance * Math.tan(angle));
    }
    private Vector3f atScreenFraction(double screenFraction) {
        return this.line.pointAt(screenFraction * this.clipFrom.w / ((1.0 - screenFraction) * this.clipTo.w + screenFraction * this.clipFrom.w));
    }
}
