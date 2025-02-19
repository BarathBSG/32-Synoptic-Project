public class Plastic extends RecyclableItem{
    private final int PricePerKG;

    public Plastic(double weight) throws InvalidWeightException{
        super(weight);
        this.PricePerKG = 3000;
    }

    public int getPricePerKG(){
        return this.PricePerKG;
    }

    @Override
    public double calculateValue(){
        return (this.getWeight() * this.getPricePerKG()) * this.getCashOutPerKG();
    }
}
