function [] = plotAvgServiceTime()

    %plotGenericResult(1, 5, 'Service Time (sec)', 'ALL_APPS', 0);
    plotGenericResult(1, 5, 'Service Time (msec)', 'REMOTE_HEALTHCARE', 0, 'REMOTE HEALTHCARE');
    %plotGenericResult(1, 5, 'Service Time (msec)', 'COGNITIVE_ASSISTANCE', 0, 'COGNITIVE ASSISTANCE');
    %plotGenericResult(1, 5, 'Service Time (msec)', 'AUGMENTED_REALITY', 0, 'AUGMENTED REALITY');
    %plotGenericResult(1, 5, 'Service Time for Health App (sec)', 'HEALTH_APP', 0);
    %plotGenericResult(1, 5, 'Service Time for Infotainment App (sec)', 'INFOTAINMENT_APP', 0);
    %plotGenericResult(1, 5, 'Service Time for Heavy Comp. App (sec)', 'HEAVY_COMP_APP', 0);
    %{
    plotGenericResult(2, 5, 'Service Time on Fog (sec)', 'ALL_APPS', 0);
    plotGenericResult(2, 5, {'Service Time on Fog';'for Augmented Reality App (sec)'}, 'AUGMENTED_REALITY', 0);
    plotGenericResult(2, 5, 'Service Time on Fog for Health App (sec)', 'HEALTH_APP', 0);
    plotGenericResult(2, 5, {'Service Time on Fog';'for Infotainment App (sec)'}, 'INFOTAINMENT_APP', 0);
    plotGenericResult(2, 5, {'Service Time on Fog';'for Heavy Comp. App (sec)'}, 'HEAVY_COMP_APP', 0);

    plotGenericResult(3, 5, 'Service Time on Cloud (sec)', 'ALL_APPS', 0);
    plotGenericResult(3, 5, {'Service Time on Cloud';'for Augmented Reality App (sec)'}, 'AUGMENTED_REALITY', 0);
    plotGenericResult(3, 5, 'Service Time on Cloud for Health App (sec)', 'HEALTH_APP', 0);
    plotGenericResult(3, 5, {'Service Time on Cloud';'for Infotainment App (sec)'}, 'INFOTAINMENT_APP', 0);
    plotGenericResult(3, 5, {'Service Time on Cloud';'for Heavy Comp. App (sec)'}, 'HEAVY_COMP_APP', 0);
    %}
end
