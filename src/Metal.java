public class Metal extends RecyclableItem{
    private final int PricePerKG;

    public Metal(double weight) throws InvalidWeightException{
        super(weight);
        this.PricePerKG = 5000;
    }

    public int getPricePerKG(){
        return this.PricePerKG;
    }

    @Override
    public double calculateValue(){
        return (this.getWeight() * this.getPricePerKG()) * this.getCashOutPerKG();
    }
}