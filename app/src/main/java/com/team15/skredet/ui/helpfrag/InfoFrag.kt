import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.team15.skredet.R
import com.team15.skredet.SharedViewModel
import com.team15.skredet.addBottomDots
import com.team15.skredet.ui.helpfrag.HelpFragment
import kotlinx.android.synthetic.main.infopage0.*
import kotlinx.android.synthetic.main.infopage1.*
import kotlinx.android.synthetic.main.infopage2.*
import kotlinx.android.synthetic.main.infopage3.*
import kotlinx.android.synthetic.main.infopage4.*
import kotlinx.android.synthetic.main.infopage5.*
import kotlinx.android.synthetic.main.infopage6.*
import kotlinx.android.synthetic.main.infopage7.*
import kotlinx.android.synthetic.main.infopage8.*

/**
 * Populates the different layouts with appropriate info
 * @author Permki
 */
class InfoFrag(private var index: Int, private var viewModel: SharedViewModel) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(getLayout(), container, false)

    /**
     * gets hold of video-uri and sets listener
     * @author Permki
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { addBottomDots(it, getDotsLayout(), (index), HelpFragment.NUM_PAGES) }
        if (index != 0) {
            val videoView = getVideoView()
            videoView.setVideoURI(Uri.parse("android.resource://" + activity?.packageName + "/" + getVideoID()))
            videoView.start()
            videoView.setOnPreparedListener { mp -> mp.isLooping = true }
        }
        if (index == HelpFragment.NUM_PAGES - 1) finishbutton.setOnClickListener { viewModel.infoPageShown.value = true }
    }

    /**
     * Self-explanatory
     * @author Permki
     */
    private fun getDotsLayout(): LinearLayout =
        when (index) {
            1 -> layout_dots1
            2 -> layout_dots2
            3 -> layout_dots3
            4 -> layout_dots4
            5 -> layout_dots5
            6 -> layout_dots6
            7 -> layout_dots7
            8 -> layout_dots8
            else -> layout_dots0
        }

    /**
     * Self-explanatory
     * @author Permki
     */
    private fun getVideoView(): VideoView {
        return when (index) {
            2 -> p2video
            3 -> p3video
            4 -> p4video
            5 -> p5video
            6 -> p6video
            7 -> p7video
            8 -> p8video
            else -> p1video
        }
    }

    /**
     * Self-explanatory
     * @author Permki
     */
    private fun getVideoID(): Int {
        return when (index) {
            2 -> R.raw.pressonmap
            3 -> R.raw.gotodetails
            4 -> R.raw.favorites
            5 -> R.raw.lists
            6 -> R.raw.settings
            7 -> R.raw.nowifi
            8 -> R.raw.infovid
            else -> R.raw.mapoverview
        }
    }

    /**
     * Self-explanatory
     * @author Permki
     */
    private fun getLayout(): Int {
        return when (index) {
            1 -> R.layout.infopage1
            2 -> R.layout.infopage2
            3 -> R.layout.infopage3
            4 -> R.layout.infopage4
            5 -> R.layout.infopage5
            6 -> R.layout.infopage6
            7 -> R.layout.infopage7
            8 -> R.layout.infopage8
            else -> R.layout.infopage0
        }
    }
}