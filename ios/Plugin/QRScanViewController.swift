import Foundation

public class QRScanViewController: UIViewController {
    var CameraView: UIView!
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        let rect = CGRect(x: 0, y: 0, width: 500, height: 500)
        CameraView = UIView(frame: rect)
        CameraView.backgroundColor = .clear
        
        self.view.addSubview(CameraView)
    }
}
