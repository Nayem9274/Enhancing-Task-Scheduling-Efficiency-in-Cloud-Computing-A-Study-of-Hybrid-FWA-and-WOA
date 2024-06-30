
    # cd /Users/macbook/study_materials/thesis/our\ project/cloudsim-plus-leader-election ; /usr/bin/env /Users/macbook/Library/Java/JavaVirtualMachines/openjdk-18.0.1.1/Contents/Home/bin/java @/var/folders/pw/vpl3k93929v2lp_4jzcnrjh00000gn/T/cp_bbypnjjrfy9ijmxp0z4yiid25.argfile edu.buet.thesis.le.ga.DatacenterBrokerHeuristicExample $task

tasks=(100 200 300 400 500 600 700 800 900 1000)
for task in ${tasks[@]}
do
    cd /Users/macbook/study_materials/thesis/our\ project/cloudsim-plus-leader-election ; /usr/bin/env /Users/macbook/Library/Java/JavaVirtualMachines/openjdk-18.0.1.1/Contents/Home/bin/java @/var/folders/pw/vpl3k93929v2lp_4jzcnrjh00000gn/T/cp_bbypnjjrfy9ijmxp0z4yiid25.argfile edu.buet.thesis.le.csa.DatacenterBrokerHeuristicExample $task
done