public abstract class RecyclableItem {
    //abstract class for recyclable items that plastic and metal branch from, with inherited properties
    private final double weight;
    private final double CashOutPerKG;

    public RecyclableItem(double weight) throws InvalidWeightException { //constructor for material and its weight
        if (weight > 500 || weight < 0)
            throw new InvalidWeightException("invalid - weight too high");
        else {
            this.weight = weight;
        }
        this.CashOutPerKG = 0.95;
    }

    public double getWeight(){
        return this.weight;
    }

    public double getCashOutPerKG(){
        return this.CashOutPerKG;
    }

    public abstract double calculateValue();
}
