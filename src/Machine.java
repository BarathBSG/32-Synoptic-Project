public class Machine {
    private final double CurrentPlasticWeight;
    private final double MaxPlasticWeight;
    private final double CurrentMetalWeight;
    private final double MaxMetalWeight;
    private final double CurrentE_WasteWeight;
    private final double MaxE_WasteWeight;
    private final int CurrentFunds;
    private final int MaximumFunds;

    public Machine(double CurrentPlasticWeight, double CurrentMetalWeight, double CurrentE_Waste, int CurrentFunds){
        this.CurrentPlasticWeight = CurrentPlasticWeight;
        this.MaxPlasticWeight = 15;
        this.CurrentMetalWeight = CurrentMetalWeight;
        this.MaxMetalWeight = 30;
        this.CurrentE_WasteWeight = CurrentE_Waste;
        this.MaxE_WasteWeight = 5;
        this.CurrentFunds = CurrentFunds;
        this.MaximumFunds = 25000;
    }

    public double getCurrentPlasticWeight() {
        return CurrentPlasticWeight;
    }

    public double getMaxPlasticWeight(){
        return MaxPlasticWeight;
    }

    public double getCurrentMetalWeight(){
        return CurrentMetalWeight;
    }

    public double getMaxMetalWeight(){
        return MaxMetalWeight;
    }

    public double getCurrentE_WasteWeight(){
        return CurrentE_WasteWeight;
    }

    public double getMaxE_WasteWeight(){
        return MaxE_WasteWeight;
    }

    public int getCurrentFunds(){
        return CurrentFunds;
    }

    public int getMaximumFunds(){
        return MaximumFunds;
    }
}
