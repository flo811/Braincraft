package braindraft.model.network;

import java.util.List;

/**
 *
 * @author flo
 */
public class HiddenLayer extends Layer<HiddenNeuron> {

    public HiddenLayer(final List<HiddenNeuron> hiddenNeurons) {
        super(hiddenNeurons);
    }

    public void activate() {
        neurons.forEach(HiddenNeuron::activate);
    }
}
