package nl.tudelft.simulation.simport.terminal;

import nl.tudelft.simulation.simport.model.PortModel;

/**
 * Terminal models a deepsea terminal.
 * <p>
 * Copyright (c) 2025-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TerminalStandard extends Terminal
{
    /** The transshipment fraction for import containers. */
    private double transshipmentFractionImport;

    /** The transshipment fraction for export containers. */
    private double transshipmentFractionExport;

    /** The modal split for import containers. */
    private ModalSplit modalSplitImport;

    /** The modal split for export containers. */
    private ModalSplit modalSplitExport;

    /**
     * Create a new terminal for the port model.
     * @param id the id of the terminal
     * @param model the port model
     * @param lat latitude
     * @param lon longitude
     */
    public TerminalStandard(final String id, final PortModel model, final double lat, final double lon)
    {
        super(id, model, lat, lon);
        model.addTerminal(this);
        if (model.isInteractive())
            new TerminalAnimation(this, model.getSimulator());
    }

    /*-
    public void addImportContainers(final List<Container> containerList)
    {
        StreamInterface rng = this.model.getDefaultStream();
        for (var container : containerList)
        {
            this.containersImport.add(container);
            var r = rng.nextDouble();
            if (r < this.modalSplitImport.cumulativeTruckFraction())
            {
                container.setMode(TransportMode.TRUCK);
                this.truckContainersImport.add(container);
            }
            else if (r < this.modalSplitImport.cumulativeBargeFraction())
                container.setMode(TransportMode.BARGE);
            else if (r < this.modalSplitImport.cumulativeRailFraction())
                container.setMode(TransportMode.RAIL);
            else
                container.setMode(TransportMode.SHORTSEA);
            this.teu += (container.getSize() > 20) ? 2 : 1;
        }
    }

    public void removeExportContainers(final List<Container> containerList)
    {
        for (var container : containerList)
        {
            this.teu -= (container.getSize() > 20) ? 2 : 1;
        }
    }
    */

    /**
     * Return the modal split for import containers for this terminal.
     * @return the modal split for import containers for this terminal
     */
    public ModalSplit getModalSplitImport()
    {
        return this.modalSplitImport;
    }

    /**
     * Set the modal split for import containers for this terminal.
     * @param modalSplitImport the modal split for import containers for this terminal
     */
    public void setModalSplitImport(final ModalSplit modalSplitImport)
    {
        this.modalSplitImport = modalSplitImport;
    }

    /**
     * Return the modal split for export containers for this terminal.
     * @return the modal split for export containers for this terminal
     */
    public ModalSplit getModalSplitExport()
    {
        return this.modalSplitExport;
    }

    /**
     * Set the modal split for export containers for this terminal.
     * @param modalSplitExport the modal split for export containers for this terminal
     */
    public void setModalSplitExport(final ModalSplit modalSplitExport)
    {
        this.modalSplitExport = modalSplitExport;
    }

    @Override
    public String toString()
    {
        return "Terminal [id=" + getId() + "]";
    }

}
