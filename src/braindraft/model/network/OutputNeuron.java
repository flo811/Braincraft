package braindraft.model.network;

import braindraft.model.ActivationFunctions;

/**
 *
 * @author flo
 */
public class OutputNeuron extends VirtualNeuron {

    private double expected;

    public OutputNeuron(final String name, final double weightRangeStartMin, final double weightRangeStartMax,
            final ActivationFunctions activationFunction, final double learningRate,
            final double bias, final Layer<? extends Outputable>... previousLayer) {
        super(name, weightRangeStartMin, weightRangeStartMax, activationFunction, learningRate, bias, previousLayer);
    }

    @Override
    public void calculateErrorAndUpdateWeight() {
        error = (expected - output) * activationFunction.getActivationFunction().applyDerivate(weightedSum);
        biasWeight += learningRate * error * bias;
        entriesWeight.entrySet().forEach(entry -> entry.setValue(entry.getValue() + learningRate * error * entry.getKey().getOutput()));
    }

    public double getExpected() {
        return expected;
    }

    public void setExpected(final double expected) {
        this.expected = expected;
    }
}
